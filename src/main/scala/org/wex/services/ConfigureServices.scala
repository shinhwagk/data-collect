package org.wex.services

import java.time.{LocalDate, LocalTime}
import scala.io.Source

/**
  * Created by zhangxu on 2016/8/10.
  */

object ConfigureServices {

  val cpus = Runtime.getRuntime().availableProcessors()


  import DataTransformServices._
  import spray.json._

  private val _path_conf = "conf"
  private val _sql_file_path_conf = s"""${_path_conf}/sqls"""
  private val _dbsource_conf = s"""${_path_conf}/dbsource.json"""
  private val _collect_conf = s"""${_path_conf}/collect.json"""

  def _conf_collect_list: List[ConfigureCollect] = {
    try {
      Source.fromFile(_collect_conf).mkString.parseJson.convertTo[List[ConfigureCollect]]
    } catch {
      case ex: Exception => throw new Exception(_collect_conf + s" configure error: ${ex.getMessage}")
    }
  }

  def _conf_db_source_list: List[ConfigureDbSource] = {
    Source.fromFile(_dbsource_conf).mkString.parseJson.convertTo[List[ConfigureDbSource]]
  }

  def getSqlTextBySqlFile(name: String): String = Source.fromFile(s"${_sql_file_path_conf}/${name}.sql").mkString

  def _generateConfigureRunningBySourcedb(cc: ConfigureCollect): List[ConfigureRunning] = {
    val targetdb = _conf_db_source_list.filter(db => db.alias == cc.targetdb).head
    val targetDBSource = DBSource(targetdb.jdbc, targetdb.user, targetdb.password)
    val sql = getSqlTextBySqlFile(cc.sqlfile)
    cc.sourcedb match {
      case Nil =>
        _conf_db_source_list.map { db =>
          ConfigureRunning(cc.name, sql, cc.table, DBSource(db.jdbc, db.user, db.password), targetDBSource)
        }
      case _ =>
        cc.sourcedb.map { c =>
          _conf_db_source_list
            .filter(_.alias == c)
            .headOption match {
            case None => throw new Exception("db:" + c + " no exist。")
            case Some(db) => ConfigureRunning(cc.name, sql, cc.table, DBSource(db.jdbc, db.user, db.password), targetDBSource)
          }
        }
    }
  }
}

case class ConfigureDbSource(alias: String, jdbc: String, user: String, password: String)

case class ConfigureCollect(name: String, sqlfile: String, timer: String, table: String, sourcedb: List[String], targetdb: String, status: Int)

case class ConfigureRunning(name: String, sql: String, table: String, sourcedb: DBSource, targetdb: DBSource)

case class DBSource(jdbc: String, user: String, password: String)


