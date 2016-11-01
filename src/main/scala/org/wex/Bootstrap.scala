package org.wex

import java.sql.Connection
import java.util.Date
import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.OverflowStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.{Sink, Source}
import org.apache.logging.log4j.LogManager
import org.quartz.CronExpression
import org.wex.services.Config.RunningConfig
import org.wex.services._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Created by zhangxu on 2016/8/10.
  */
object Bootstrap extends App {
  import org.wex.services.ActorSystemServices._
  import org.wex.services.DatabaseServices._
  import org.wex.services.SystemConfigure._

  generatePid()

  val logger = LogManager.getLogger(this.getClass.getName);

  def funcResultSetIterator(sourceConn: Connection, sql: String, cols: Seq[String]) = {
    val stmt = sourceConn.createStatement()
    generateQueryIterator(stmt.executeQuery(sql), cols)
  }

  val _LoadTODatabase = (cr: RunningConfig) => Future {
    val sourceConn: Connection = DatabaseServices.createConnection(cr.sourceDs)
    val targetConn: Connection = DatabaseServices.createConnection(cr.targetDs)

    val (stmt, cols) = getColumnListBySql(sourceConn, cr.sql)
    stmt.close()

    val insertSql = insertSqlTemplate(cols, cr.table)

    val queryIteratorStream = funcResultSetIterator(sourceConn, cr.sql, cols).toStream

    val stmt2 = targetConn.prepareStatement(insertSql)

    logger.info(s"exec task: ${cr.name} >> ${cr.sourceDs.alias} -> ${cr.targetDs.alias}, Source data number ${queryIteratorStream.size}")

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
          closeConnection(sourceConn)
          closeConnection(targetConn)
          logger.info(s"exec task: ${cr.name} >> ${cr.sourceDs.alias} -> ${cr.targetDs.alias}. Success")
        case Failure(ex) =>
          closeConnection(sourceConn)
          closeConnection(targetConn)
          logger.error(s"exec task: ${cr.name} >> ${cr.sourceDs.alias} -> ${cr.targetDs.alias}. Failure. error:${ex.getMessage}")
      }
  }

  Source.tick(0.seconds, 1.seconds, ())
    .map { p => logger.info("query executable task"); p }
    .map(_ => ConfigureServices._conf_collect_list).withAttributes(supervisionStrategy(decider(logger)))
    .mapConcat(ccl => ccl)
    .filter(ccl => new CronExpression(ccl.cron).isSatisfiedBy(new Date())).withAttributes(supervisionStrategy(decider(logger)))
    .filter(ccl => ccl.status == 0)
    .map { ccl => logger.info(s"exec task ${ccl.name}"); ccl }
    .map(ccl => ConfigureServices._generateConfigureRunningBySourcedb(ccl)).withAttributes(supervisionStrategy(decider(logger)))
    .mapConcat(c => c).async.buffer(100, OverflowStrategy.backpressure)
    .map { c => logger.info(s"exec task: ${c.name} >> ${c.sourceDs.alias} -> ${c.targetDs.alias}"); c }
    .mapAsync(SystemConfigure.cpus)(_LoadTODatabase(_)).withAttributes(supervisionStrategy(decider(logger)))
//    .withAttributes(supervisionStrategy(resumingDecider))
    .runWith(Sink.ignore)
    .onComplete {
      case Success(_) => logger.info("data collect success")
      case Failure(ex) => logger.error("data collect failure")
    }
}


