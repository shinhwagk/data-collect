package org.wex.services

/**
  * Created by zhangxu on 2016/9/2.
  */
object Config {

  case class DbSourceConfig(alias: String, jdbc: String, user: String, password: String)

  case class CollectConfig(name: String, sqlfile: String, cron: String, table: String, sourcedb: List[String], targetdb: String, status: Int)

  case class RunningConfig(name: String, sql: String, table: String, sourceDs: DBSource, targetDs: DBSource)

  case class DBSource(alias: String, jdbc: String, user: String, password: String)

}
