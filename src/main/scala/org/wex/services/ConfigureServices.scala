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
    Source.fromFile(_collect_conf).mkString.parseJson.convertTo[List[ConfigureCollect]]
  }

  def _conf_db_source_list: List[ConfigureDbSource] = {
    Source.fromFile(_dbsource_conf).mkString.parseJson.convertTo[List[ConfigureDbSource]]
  }

  def getSqlBySqlFile(name: String): String = Source.fromFile(s"${_sql_file_path_conf}/${name}.sql").mkString

  def _exec_list: List[ConfigureRunning] = {
    _conf_collect_list
      //      .filter(c => CommonServices.timerFilter(c.timer))
      .filter(_.status == 0)
      .flatMap { ccl =>
        val sql = getSqlBySqlFile(ccl.sqlfile)
        val targetdb = _conf_db_source_list.filter(db => db.alias == ccl.targetdb).head
        val targetDBSource = DBSource(targetdb.jdbc, targetdb.user, targetdb.password)
        ccl.sourcedb match {
          case Nil =>
            _conf_db_source_list.map { b =>
              ConfigureRunning(ccl.name, sql, ccl.table, DBSource(b.jdbc, b.user, b.password), targetDBSource)
            }
          case _ =>
            ccl.sourcedb.map { c =>
//              val optionDb: Option[ConfigureDbSource] = _conf_db_source_list.filter(_.alias == c).headOption
              _conf_db_source_list
                .filter(_.alias == c)
                .headOption match {
                case None => throw new Exception("db:" + c + " no exist。")
                case Some(db) => ConfigureRunning(ccl.name, sql, ccl.table, DBSource(db.jdbc, db.user, db.password), targetDBSource)
              }
            }
        }
      }
  }
}

case class ConfigureDbSource(alias: String, jdbc: String, user: String, password: String)

case class ConfigureCollect(name: String, sqlfile: String, timer: String, table: String, sourcedb: List[String], targetdb: String, status: Int)

case class ConfigureRunning(name: String, sql: String, table: String, sourcedb: DBSource, targetdb: DBSource)

case class DBSource(jdbc: String, user: String, password: String)


