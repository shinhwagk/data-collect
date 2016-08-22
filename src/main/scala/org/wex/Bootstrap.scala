package org.wex

import java.sql.{Connection, PreparedStatement, ResultSet, Statement}

import akka.stream.scaladsl.{Sink, Source}
import akka.stream.{ActorAttributes, OverflowStrategy, Supervision}
import org.apache.logging.log4j.{LogManager, Logger}
import org.wex.services.{CommonServices, ConfigureRunning, ConfigureServices}

import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import ActorAttributes.supervisionStrategy
import akka.stream.Supervision.resumingDecider

/**
  * Created by zhangxu on 2016/8/10.
  */
object Bootstrap extends App {

  var num = 0

  import org.wex.services.ActorSystemServices._

  val log = LogManager.getLogger(this.getClass.getName);

  def funcColumns(sourceConn: Connection, sql: String): (Statement, IndexedSeq[String]) = {
    val preStmt = sourceConn.prepareStatement(sql)
    val meta = preStmt.getMetaData
    (preStmt, (1 to meta.getColumnCount).map(meta.getColumnName(_)))
  }

  def funcResultSetIterator(sourceConn: Connection, sql: String, cols: Seq[String]): Stream[Map[String, Object]] = {
    val stmt = sourceConn.createStatement()
    QueryIterator(stmt.executeQuery(sql), cols).toStream
  }

  def sqltemplate(cols: Seq[String], sql: String, targetTable: String): String = {
    val size = cols.size
    s"INSERT INTO ${targetTable}(${cols.reverse.mkString(",")}) VALUES " + (1 to size).map(_ => "?").mkString("(", ",", ")")
  }

  val _LoadTODatabase = (cr: ConfigureRunning) => Future {
    val sourceConn: Connection = CommonServices.createOracleConnection(cr.sourcedb)
    val targetConn: Connection = CommonServices.createOracleConnection(cr.targetdb)

    val (stmt, cols) = funcColumns(sourceConn, cr.sql)
    stmt.close()

    val insertSql = sqltemplate(cols, cr.sql, cr.table)

    val stmt2: PreparedStatement = targetConn.prepareStatement(insertSql)

    val queryIterator: Stream[Map[String, Object]] = funcResultSetIterator(sourceConn, cr.sql, cols)

    Source(queryIterator)
      .map { rs =>
        val nrs = rs.toArray
        (1 to nrs.size).foreach(cnt => stmt2.setObject(cnt, nrs(cnt - 1)._2))
        stmt2.execute()
        stmt2.clearParameters()
      }
      .runWith(Sink.ignore)
      .onComplete {
        case Success(_) => sourceConn.close(); targetConn.close(); log.info("task: " + cr.name + ". Success")
        case Failure(ex) => sourceConn.close(); targetConn.close(); log.error("task: " + cr.name + s". Failure. error:${ex.getMessage}")
      }
  }

  Source.tick(0.seconds, 3.seconds, ())
    .map { p => log.info("query executable task"); p }
    .map { _ => ConfigureServices._exec_list }.withAttributes(ActorAttributes.supervisionStrategy(decider2(log)))
    .filter(_.size > 0)
    .mapConcat(f => f).async.buffer(500, OverflowStrategy.backpressure)
    .map { p => log.info(s"exec task: ${p.name}"); p }
    .mapAsync(ConfigureServices.cpus)(_LoadTODatabase(_)).withAttributes(supervisionStrategy(resumingDecider))
    .runWith(Sink.ignore)
}

case class QueryIterator(val rs: ResultSet, cols: Seq[String]) extends Iterator[Map[String, Object]] {

  override def hasNext: Boolean = rs.next()

  override def next(): Map[String, Object] = cols.map(p => (p, rs.getObject(p))).toMap
}
