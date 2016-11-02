package org.wex.services

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.wex.services.Config._
import spray.json.DefaultJsonProtocol

/**
  * Created by zhangxu on 2016/8/4.
  */
object DataTransformServices extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val _object_collent_m_Format = jsonFormat7(CC)
  implicit val _object_db_source_m_Format = jsonFormat4(DbSourceConfig)
}
