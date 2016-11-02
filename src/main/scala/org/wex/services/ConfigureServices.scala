package org.wex.services

import org.wex.services.Config._
import scala.io.Source

/**
  * Created by zhangxu on 2016/8/10.
  */

object ConfigureServices {

  import DataTransformServices._
  import spray.json._

  private val _path_conf = "conf"
  private val _sql_file_path_conf = s"""${_path_conf}/sqls"""
  private val _dbsource_conf = s"""${_path_conf}/dbsource.json"""
  private val _collect_conf = s"""${_path_conf}/collect.json"""

  def _conf_collect_list: List[CollectConfig] = {
    try {
      val cc: List[CC] = Source.fromFile(_collect_conf).mkString.parseJson.convertTo[List[CC]]
      cc.map(c => CollectConfig(c.name, c.sqlfile, c.cron, c.table, c.sourcedb, c.targetdb, c.status))
    } catch {
      case ex: Exception => throw new Exception(_collect_conf + s" collect config error: ${ex.getMessage}")
    }
  }

  def _conf_db_source_list: List[DbSourceConfig] = {
    try {
      Source.fromFile(_dbsource_conf).mkString.parseJson.convertTo[List[DbSourceConfig]]
    } catch {
      case ex: Exception => throw new Exception(_collect_conf + s" dbsource config error: ${ex.getMessage}")
    }
  }

  def getSqlTextBySqlFile(name: String): String = Source.fromFile(s"${_sql_file_path_conf}/${name}").mkString

  def _generateConfigureRunningBySourcedb(cc: CollectConfig): List[RunningConfig] = {
    val taskId = cc.id
    val targetDb = _conf_db_source_list.filter(db => db.alias == cc.targetdb).head
    val targetDBSource = DBSource(targetDb.alias, targetDb.jdbc, targetDb.user, targetDb.password)
    val sql = getSqlTextBySqlFile(cc.sqlfile)
    cc.sourcedb match {
      case Nil =>
        _conf_db_source_list.map { db =>
          RunningConfig(taskId, cc.name, sql, cc.table, DBSource(db.alias, db.jdbc, db.user, db.password), targetDBSource)
        }
      case _ =>
        cc.sourcedb.map { c =>
          _conf_db_source_list
            .filter(_.alias == c)
            .headOption match {
            case None => throw new Exception("db:" + c + " no exist。")
            case Some(db) => RunningConfig(taskId, cc.name, sql, cc.table, DBSource(db.alias, db.jdbc, db.user, db.password), targetDBSource)
          }
        }
    }
  }
}



