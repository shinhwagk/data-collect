package org.wex

import java.sql.Connection
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.OverflowStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.{Sink, Source}
import org.apache.logging.log4j.LogManager
import org.wex.services.{CommonServices, ConfigureRunning, ConfigureServices}
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by zhangxu on 2016/8/10.
  */
object Bootstrap extends App {

  import org.wex.services.ActorSystemServices._
  import org.wex.services.DatabaseServices._

  val log = LogManager.getLogger(this.getClass.getName);

  def funcResultSetIterator(sourceConn: Connection, sql: String, cols: Seq[String]) = {
    val stmt = sourceConn.createStatement()
    generateQueryIterator(stmt.executeQuery(sql), cols)
  }

  val _LoadTODatabase = (cr: ConfigureRunning) => Future {
    val sourceConn: Connection = CommonServices.createOracleConnection(cr.sourcedb)
    val targetConn: Connection = CommonServices.createOracleConnection(cr.targetdb)

    val (stmt, cols) = getColumnListBySql(sourceConn, cr.sql)
    stmt.close()

    val insertSql = insertSqlTemplate(cols, cr.table)

    val queryIteratorStream = funcResultSetIterator(sourceConn, cr.sql, cols).toStream

    val stmt2 = targetConn.prepareStatement(insertSql)

    Source(queryIteratorStream)
      .map { rs =>
        val nrs = rs.toArray
        (1 to nrs.size).foreach(cnt => stmt2.setObject(cnt, nrs(cnt - 1)._2))
        stmt2.execute()
        stmt2.clearParameters()
      }
      .runWith(Sink.ignore)
      .onComplete {
        case Success(_) =>
          closeOracleConnection(sourceConn)
          closeOracleConnection(targetConn)
          log.info("task: " + cr.name + ". Success")
        case Failure(ex) =>
          closeOracleConnection(sourceConn)
          closeOracleConnection(targetConn)
          log.error("task: " + cr.name + s". Failure. error:${ex.getMessage}")
      }
  }

  Source.tick(0.seconds, 1.minutes, ())
    .map { p => log.info("query executable task"); p }
    .map(_ => ConfigureServices._conf_collect_list).withAttributes(supervisionStrategy(decider2(log)))
    .mapConcat(ccl => ccl)
    .filter(ccl => CommonServices.timerFilter(ccl.timer))
    .filter(ccl => ccl.status == 0)
    .map { ccl => log.info(s"exec task ${ccl.name}"); ccl }
    .map(ccl => ConfigureServices._generateConfigureRunningBySourcedb(ccl)).withAttributes(supervisionStrategy(decider2(log)))
    .mapConcat(c => c).async.buffer(20, OverflowStrategy.backpressure)
    .map { c => log.info(s"exec task ${c.name} on ${c.sourcedb.jdbc}/${c.sourcedb.user} -> ${c.targetdb.jdbc}/${c.targetdb.user}.${c.table} "); c }
    .mapAsync(ConfigureServices.cpus)(_LoadTODatabase(_)).withAttributes(supervisionStrategy(resumingDecider))
    .runWith(Sink.ignore)
    .onComplete {
      case Success(_) => log.info("data collect success")
      case Failure(ex) => log.error("data collect failure")
    }

}


