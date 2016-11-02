package org.wex.services

import scala.util.Random

/**
  * Created by zhangxu on 2016/9/2.
  */
object Config {

  val random = new Random()

  case class DbSourceConfig(alias: String, jdbc: String, user: String, password: String)

  case class CC(name: String, sqlfile: String, cron: String, table: String, sourcedb: List[String], targetdb: String, status: Int)

  case class CollectConfig(name: String, sqlfile: String, cron: String, table: String, sourcedb: List[String], targetdb: String, status: Int, id: Int = random.nextInt(10000))

  case class RunningConfig(taskId: Int, name: String, sql: String, table: String, sourceDs: DBSource, targetDs: DBSource, id: Int = random.nextInt(10000))

  case class DBSource(alias: String, jdbc: String, user: String, password: String)

}
