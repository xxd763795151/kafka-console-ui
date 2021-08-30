package kafka.console

import org.slf4j.{Logger, LoggerFactory}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:30:14
 * */
trait Logging {

    protected lazy val log : Logger = LoggerFactory.getLogger(this.getClass)
}
