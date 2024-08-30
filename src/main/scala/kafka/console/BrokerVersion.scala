package kafka.console

import org.apache.kafka.clients.NodeApiVersions
import org.apache.kafka.common.message.ApiVersionsResponseData.ApiVersion

import java.util.Collections
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.reflect.runtime.universe._
import scala.reflect.runtime.{universe => ru}


/**
 * broker version with api version.
 *
 * @author: xuxd
 * @since: 2024/8/29 16:12
 * */
class BrokerVersion(val apiVersion: Int, val brokerVersion: String)

object BrokerVersion {

  val define: List[BrokerVersion] = List(
    new BrokerVersion(33, "0.11"),
    new BrokerVersion(37, "1.0"),
    new BrokerVersion(42, "1.1"),
    new BrokerVersion(43, "2.2"),
    new BrokerVersion(44, "2.3"),
    new BrokerVersion(47, "2.4~2.5"),
    new BrokerVersion(49, "2.6"),
    new BrokerVersion(57, "2.7"),
    new BrokerVersion(64, "2.8"),
    new BrokerVersion(67, "3.0~3.4"),
    new BrokerVersion(68, "3.5~3.6"),
    new BrokerVersion(74, "3.7"),
    new BrokerVersion(75, "3.8")
  )

  def getDefineWithJavaList(): java.util.List[BrokerVersion] = {
    define.toBuffer.asJava
  }

  def guessBrokerVersion(nodeVersion: NodeApiVersions): String = {
    //      if (nodeVersion.)
    val unknown = "unknown";
    var guessVersion = unknown
    var maxApiKey: Short = -1
    if (nodeVersion.toString().contains("UNKNOWN")) {
      val unknownApis = getFieldValueByName(nodeVersion, "unknownApis")
      unknownApis match {
        case Some(unknownApis: java.util.List[ApiVersion]) => {
          if (unknownApis.size > 0) {
            maxApiKey = unknownApis.get(unknownApis.size() - 1).apiKey()
          }
        }
        case _ => -1
      }
    }
    if (maxApiKey < 0) {
      val versions = new java.util.ArrayList[ApiVersion](nodeVersion.allSupportedApiVersions().values())
      Collections.sort(versions, (o1: ApiVersion, o2: ApiVersion) => o2.apiKey() - o1.apiKey)
      maxApiKey = versions.get(0).apiKey()
    }
    if (maxApiKey > 0) {

      if (maxApiKey > define.last.apiVersion) {
        guessVersion = "> " + define.last.brokerVersion
      } else if (maxApiKey < define.head.apiVersion) {
        guessVersion = "< " + define.head.brokerVersion
      } else {
        for (i <- define.indices) {
          if (maxApiKey <= define(i).apiVersion && guessVersion == unknown) {
            guessVersion = define(i).brokerVersion
          }
        }

      }
    }

    guessVersion
  }

  def getFieldValueByName(obj: Object, fieldName: String): Option[Any] = {
    val runtimeMirror = ru.runtimeMirror(obj.getClass.getClassLoader)
    val instanceMirror = runtimeMirror.reflect(obj)
    val typeOfObj = instanceMirror.symbol.toType

    // 查找名为 fieldName 的字段
    val fieldSymbol = typeOfObj.member(newTermName(fieldName)).asTerm

    // 检查字段是否存在并且不是私有字段
    if (fieldSymbol.isPrivate || fieldSymbol.isPrivateThis) {
      //      None // 如果字段是私有的，返回 None
      val fieldMirror = runtimeMirror.reflect(obj).reflectField(fieldSymbol)
      Some(fieldMirror.get)
    } else {
      // 反射获取字段值
      val fieldMirror = instanceMirror.reflectField(fieldSymbol)
      Some(fieldMirror.get)
    }
  }
}