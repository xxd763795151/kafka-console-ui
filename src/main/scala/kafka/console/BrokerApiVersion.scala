package kafka.console

import com.xuxd.kafka.console.config.ContextConfigHolder
import kafka.utils.Implicits.MapExtensionMethods
import kafka.utils.Logging
import org.apache.kafka.clients._
import org.apache.kafka.clients.admin.AdminClientConfig
import org.apache.kafka.clients.consumer.internals.{ConsumerNetworkClient, RequestFuture}
import org.apache.kafka.common.Node
import org.apache.kafka.common.config.ConfigDef.ValidString.in
import org.apache.kafka.common.config.ConfigDef.{Importance, Type}
import org.apache.kafka.common.config.{AbstractConfig, ConfigDef}
import org.apache.kafka.common.errors.AuthenticationException
import org.apache.kafka.common.internals.ClusterResourceListeners
import org.apache.kafka.common.message.ApiVersionsResponseData.ApiVersionCollection
import org.apache.kafka.common.metrics.Metrics
import org.apache.kafka.common.network.Selector
import org.apache.kafka.common.protocol.Errors
import org.apache.kafka.common.requests._
import org.apache.kafka.common.utils.{KafkaThread, LogContext, Time}
import org.slf4j.{Logger, LoggerFactory}

import java.io.IOException
import java.util.{Collections, Properties}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ConcurrentLinkedQueue, TimeUnit}
import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsJava, PropertiesHasAsScala, SetHasAsScala}
import scala.util.{Failure, Success, Try}

/**
 * kafka-console-ui.
 *
 * Copy from {@link kafka.admin.BrokerApiVersionsCommand}.
 *
 * @author xuxd
 * @date 2022-01-22 15:15:57
 * */
object BrokerApiVersion{

  protected lazy val log : Logger = LoggerFactory.getLogger(this.getClass)

    def listAllBrokerApiVersionInfo(): java.util.HashMap[Node, NodeApiVersions] = {
        val res = new java.util.HashMap[Node, NodeApiVersions]()
        val adminClient = createAdminClient()
        try {
            adminClient.awaitBrokers()
            val brokerMap = adminClient.listAllBrokerVersionInfo()
            brokerMap.forKeyValue {
                (broker, versionInfoOrError) =>
                    versionInfoOrError match {
                        case Success(v) => {
                            res.put(broker, v)
                        }
                        case Failure(v) => log.error(s"${broker} -> ERROR: ${v}\n")
                    }
            }
        } finally {
            adminClient.close()
        }

        res
    }

    private def createAdminClient(): AdminClient = {
        val props = new Properties()
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, ContextConfigHolder.CONTEXT_CONFIG.get().getBootstrapServer())
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs())
        props.putAll(ContextConfigHolder.CONTEXT_CONFIG.get().getProperties())
        AdminClient.create(props)
    }

    // org.apache.kafka.clients.admin.AdminClient doesn't currently expose a way to retrieve the supported api versions.
    // We inline the bits we need from kafka.admin.AdminClient so that we can delete it.
    private class AdminClient(val time: Time,
        val client: ConsumerNetworkClient,
        val bootstrapBrokers: List[Node]) extends Logging {

        @volatile var running = true
        val pendingFutures = new ConcurrentLinkedQueue[RequestFuture[ClientResponse]]()

        val networkThread = new KafkaThread("admin-client-network-thread", () => {
            try {
                while (running)
                    client.poll(time.timer(Long.MaxValue))
            } catch {
                case t: Throwable =>
                    error("admin-client-network-thread exited", t)
            } finally {
                pendingFutures.forEach { future =>
                    try {
                        future.raise(Errors.UNKNOWN_SERVER_ERROR)
                    } catch {
                        case _: IllegalStateException => // It is OK if the future has been completed
                    }
                }
                pendingFutures.clear()
            }
        }, true)

        networkThread.start()

        private def send(target: Node,
            request: AbstractRequest.Builder[_ <: AbstractRequest]): AbstractResponse = {
            val future = client.send(target, request)
            pendingFutures.add(future)
            future.awaitDone(Long.MaxValue, TimeUnit.MILLISECONDS)
            pendingFutures.remove(future)
            if (future.succeeded())
                future.value().responseBody()
            else
                throw future.exception()
        }

        private def sendAnyNode(request: AbstractRequest.Builder[_ <: AbstractRequest]): AbstractResponse = {
            bootstrapBrokers.foreach { broker =>
                try {
                    return send(broker, request)
                } catch {
                    case e: AuthenticationException =>
                        throw e
                    case e: Exception =>
                        debug(s"Request ${request.apiKey()} failed against node $broker", e)
                }
            }
            throw new RuntimeException(s"Request ${request.apiKey()} failed on brokers $bootstrapBrokers")
        }

        private def getApiVersions(node: Node): ApiVersionCollection = {
            val response = send(node, new ApiVersionsRequest.Builder()).asInstanceOf[ApiVersionsResponse]
            Errors.forCode(response.data.errorCode).maybeThrow()
            response.data.apiKeys
        }

        /**
         * Wait until there is a non-empty list of brokers in the cluster.
         */
        def awaitBrokers(): Unit = {
            var nodes = List[Node]()
            val start = System.currentTimeMillis()
            val maxWait = 30 * 1000
            do {
                nodes = findAllBrokers()
                if (nodes.isEmpty) {
                    Thread.sleep(50)
                }
            }
            while (nodes.isEmpty && (System.currentTimeMillis() - start < maxWait))
        }

        private def findAllBrokers(): List[Node] = {
            val request = MetadataRequest.Builder.allTopics()
            val response = sendAnyNode(request).asInstanceOf[MetadataResponse]
            val errors = response.errors
            if (!errors.isEmpty) {
                log.info(s"Metadata request contained errors: $errors")
            }

            // 在3.x版本中这个方法是buildCluster 代替cluster()了
                        response.buildCluster.nodes.asScala.toList
//            response.cluster().nodes.asScala.toList
        }

        def listAllBrokerVersionInfo(): Map[Node, Try[NodeApiVersions]] =
            findAllBrokers().map { broker =>
                broker -> Try[NodeApiVersions](new NodeApiVersions(getApiVersions(broker), Collections.emptyList(), false))
            }.toMap

        def close(): Unit = {
            running = false
            try {
                client.close()
            } catch {
                case e: IOException =>
                    error("Exception closing nioSelector:", e)
            }
        }

    }

    private object AdminClient {
        val DefaultConnectionMaxIdleMs = 9 * 60 * 1000
        val DefaultRequestTimeoutMs = 5000
        val DefaultSocketConnectionSetupMs = CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG
        val DefaultSocketConnectionSetupMaxMs = CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG
        val DefaultMaxInFlightRequestsPerConnection = 100
        val DefaultReconnectBackoffMs = 50
        val DefaultReconnectBackoffMax = 50
        val DefaultSendBufferBytes = 128 * 1024
        val DefaultReceiveBufferBytes = 32 * 1024
        val DefaultRetryBackoffMs = 100

        val AdminClientIdSequence = new AtomicInteger(1)
        val AdminConfigDef = {
            val config = new ConfigDef()
                .define(
                    CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG,
                    Type.LIST,
                    Importance.HIGH,
                    CommonClientConfigs.BOOTSTRAP_SERVERS_DOC)
                .define(CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG,
                    Type.STRING,
                    ClientDnsLookup.USE_ALL_DNS_IPS.toString,
                    in(ClientDnsLookup.USE_ALL_DNS_IPS.toString,
                        ClientDnsLookup.RESOLVE_CANONICAL_BOOTSTRAP_SERVERS_ONLY.toString),
                    Importance.MEDIUM,
                    CommonClientConfigs.CLIENT_DNS_LOOKUP_DOC)
                .define(
                    CommonClientConfigs.SECURITY_PROTOCOL_CONFIG,
                    ConfigDef.Type.STRING,
                    CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL,
                    ConfigDef.Importance.MEDIUM,
                    CommonClientConfigs.SECURITY_PROTOCOL_DOC)
                .define(
                    CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG,
                    ConfigDef.Type.INT,
                    DefaultRequestTimeoutMs,
                    ConfigDef.Importance.MEDIUM,
                    CommonClientConfigs.REQUEST_TIMEOUT_MS_DOC)
                .define(
                    CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG,
                    ConfigDef.Type.LONG,
                    CommonClientConfigs.DEFAULT_SOCKET_CONNECTION_SETUP_TIMEOUT_MS,
                    ConfigDef.Importance.MEDIUM,
                    CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_DOC)
                .define(
                    CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG,
                    ConfigDef.Type.LONG,
                    CommonClientConfigs.DEFAULT_SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS,
                    ConfigDef.Importance.MEDIUM,
                    CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_DOC)
                .define(
                    CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG,
                    ConfigDef.Type.LONG,
                    DefaultRetryBackoffMs,
                    ConfigDef.Importance.MEDIUM,
                    CommonClientConfigs.RETRY_BACKOFF_MS_DOC)
                .withClientSslSupport()
                .withClientSaslSupport()
            config
        }

        class AdminConfig(originals: Map[_, _]) extends AbstractConfig(AdminConfigDef, originals.asJava, false)

        def create(props: Properties): AdminClient = {
            val properties = new Properties()
            val names = props.stringPropertyNames()
            for (name <- names.asScala.toSet) {
                properties.put(name, props.get(name).toString())
            }
            create(properties.asScala.toMap)
        }

        def create(props: Map[String, _]): AdminClient = create(new AdminConfig(props))

        def create(config: AdminConfig): AdminClient = {
            val clientId = "admin-" + AdminClientIdSequence.getAndIncrement()
            val logContext = new LogContext(s"[LegacyAdminClient clientId=$clientId] ")
            val time = Time.SYSTEM
            val metrics = new Metrics(time)
            val metadata = new Metadata(100L, 60 * 60 * 1000L, logContext,
                new ClusterResourceListeners)
            val channelBuilder = ClientUtils.createChannelBuilder(config, time, logContext)
            val requestTimeoutMs = config.getInt(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG)
            val connectionSetupTimeoutMs = config.getLong(CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG)
            val connectionSetupTimeoutMaxMs = config.getLong(CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG)
            val retryBackoffMs = config.getLong(CommonClientConfigs.RETRY_BACKOFF_MS_CONFIG)

            val brokerUrls = config.getList(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG)
            val clientDnsLookup = config.getString(CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG)
            val brokerAddresses = ClientUtils.parseAndValidateAddresses(brokerUrls, clientDnsLookup)
            metadata.bootstrap(brokerAddresses)

            val selector = new Selector(
                DefaultConnectionMaxIdleMs,
                metrics,
                time,
                "admin",
                channelBuilder,
                logContext)

            // 版本不一样，这个地方的兼容性问题也不一样了
            // 3.x版本用这个
                        val networkClient = new NetworkClient(
                            selector,
                            metadata,
                            clientId,
                            DefaultMaxInFlightRequestsPerConnection,
                            DefaultReconnectBackoffMs,
                            DefaultReconnectBackoffMax,
                            DefaultSendBufferBytes,
                            DefaultReceiveBufferBytes,
                            requestTimeoutMs,
                            connectionSetupTimeoutMs,
                            connectionSetupTimeoutMaxMs,
                            time,
                            true,
                            new ApiVersions,
                            logContext)

//            val networkClient = new NetworkClient(
//                selector,
//                metadata,
//                clientId,
//                DefaultMaxInFlightRequestsPerConnection,
//                DefaultReconnectBackoffMs,
//                DefaultReconnectBackoffMax,
//                DefaultSendBufferBytes,
//                DefaultReceiveBufferBytes,
//                requestTimeoutMs,
//                connectionSetupTimeoutMs,
//                connectionSetupTimeoutMaxMs,
//                ClientDnsLookup.USE_ALL_DNS_IPS,
//                time,
//                true,
//                new ApiVersions,
//                logContext)

            val highLevelClient = new ConsumerNetworkClient(
                logContext,
                networkClient,
                metadata,
                time,
                retryBackoffMs,
                requestTimeoutMs,
                Integer.MAX_VALUE)

            new AdminClient(
                time,
                highLevelClient,
                metadata.fetch.nodes.asScala.toList)
        }
    }
}
