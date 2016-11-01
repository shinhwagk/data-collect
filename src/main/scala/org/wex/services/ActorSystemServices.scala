package org.wex.services

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Supervision}
import org.apache.logging.log4j.Logger

/**
  * Created by zhangxu on 2016/8/2.
  */
object ActorSystemServices {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val decider: (Logger) => Supervision.Decider = (log) => {
    case ex: Exception =>
      log.error(ex.getMessage)
      Supervision.Resume
  }
}
