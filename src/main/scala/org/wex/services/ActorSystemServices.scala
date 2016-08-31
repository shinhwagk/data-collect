package org.wex.services

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import org.apache.logging.log4j.Logger

/**
  * Created by zhangxu on 2016/8/2.
  */
object ActorSystemServices {


  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  Class.forName("oracle.jdbc.driver.OracleDriver");


  val decider: (Logger, String) => Supervision.Decider = (log: Logger, task_name: String) => {
    case ex: Exception => {
      log.error("task: " + task_name + s". Failure. error:${ex.getMessage}")
      Supervision.Resume
    }
  }

  val decider2: (Logger) => Supervision.Decider = (log) => {
    case ex: Exception =>
      log.error(ex.getMessage)
      Supervision.Resume
  }
}
