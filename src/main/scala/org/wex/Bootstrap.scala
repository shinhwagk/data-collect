package org.wex

import java.sql.Connection
import java.util.Date

import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.OverflowStrategy
import akka.stream.Supervision.resumingDecider
import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
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

  implicit val logger = LogManager.getLogger(this.getClass.getName);

  def funcResultSetIterator(sourceConn: Connection, sql: String, cols: Seq[String]) = {
    val stmt = sourceConn.createStatement()
    generateQueryIterator(stmt.executeQuery(sql), cols)
  }

  val _LoadTODatabase = (cr: RunningConfig) => Future {
    try {
      val sourceConn: Connection = DatabaseServices.createConnection(cr.sourceDs)
      val targetConn: Connection = DatabaseServices.createConnection(cr.targetDs)

      val (stmt, cols) = getColumnListBySql(sourceConn, cr.sql)
      stmt.close()

      val insertSql = insertSqlTemplate(cols, cr.table)

      val queryIteratorStream = funcResultSetIterator(sourceConn, cr.sql, cols).toStream

      val stmt2 = targetConn.prepareStatement(insertSql)

      logger.info(s"exec task: ${cr.name}; id: ${cr.taskId}-${cr.id} >> ${cr.sourceDs.alias} -> ${cr.targetDs.alias}, Source data number ${queryIteratorStream.size}")

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
            logger.info(s"exec task: ${cr.name}; id: ${cr.taskId}-${cr.id} >> ${cr.sourceDs.alias} -> ${cr.targetDs.alias}. Success")
            logger.debug(cr)
          case Failure(ex) =>
            closeConnection(sourceConn)
            closeConnection(targetConn)
            logger.error(s"exec task: ${cr.name}; id: ${cr.taskId}-${cr.id} >> ${cr.sourceDs.alias} -> ${cr.targetDs.alias}. Failure. error:${ex.getMessage}")
            logger.debug(cr)
        }
    } catch {
      case ex: Exception => throw new Exception(s"exec task: ${cr.name}; id: ${cr.taskId}-${cr.id} >> ${ex.getMessage}")
    }
  }

  val queue: SourceQueueWithComplete[RunningConfig] =
    Source.queue[RunningConfig](1000, OverflowStrategy.backpressure)
      .mapAsync(SystemConfigure.cpus)(_LoadTODatabase(_)).withAttributes(supervisionStrategy(decider))
      .to(Sink.ignore)
      .run()

  Source.tick(0.seconds, 1.seconds, ())
    .map { p => logger.info("query executable task"); p }
    .map(_ => ConfigureServices._conf_collect_list).withAttributes(supervisionStrategy(decider))
    .mapConcat(ccl => ccl)
    .filter(ccl => new CronExpression(ccl.cron).isSatisfiedBy(new Date())).withAttributes(supervisionStrategy(decider))
    .filter(ccl => ccl.status == 0)
    .map { ccl =>
      logger.info(s"exec task ${ccl.name}; id: ${ccl.id}")
      logger.debug(ccl)
      ccl
    }
    .map(ccl => ConfigureServices._generateConfigureRunningBySourcedb(ccl)).withAttributes(supervisionStrategy(decider))
    .mapConcat(c => c).async.buffer(100, OverflowStrategy.backpressure)
    .map { cr =>
      logger.info(s"exec task: ${cr.name}; id: ${cr.taskId}-${cr.id} >> ${cr.sourceDs.alias} -> ${cr.targetDs.alias}")
      logger.debug(cr)
      cr
    }
    .runForeach(cr => queue offer cr)
    .onComplete {
      case Success(_) => logger.info("data collect success")
      case Failure(ex) => logger.error("data collect failure")
    }
}