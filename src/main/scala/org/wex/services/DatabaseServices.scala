package org.wex.services

import java.sql.{Connection, ResultSet, Statement}

/**
  * Created by zhangxu on 2016/8/22.
  */
object DatabaseServices {
  def generateQueryIterator(rs: ResultSet, cols: Seq[String]): Iterator[Map[String, Object]] = {
    new Iterator[Map[String, Object]] {
      override def hasNext: Boolean = rs.next()

      override def next(): Map[String, Object] = cols.map(p => (p, rs.getObject(p))).toMap
    }
  }

  def closeOracleConnection(conn: Connection): Unit = conn.close()

  def insertSqlTemplate(columns: Seq[String], table: String): String =
    s"INSERT INTO ${table}(${columns.reverse.mkString(",")}) VALUES " + (1 to columns.size).map(_ => "?").mkString("(", ",", ")")


  def getColumnListBySql(conn: Connection, sql: String): (Statement, Seq[String]) = {
    val preStmt = conn.prepareStatement(sql)
    val meta = preStmt.getMetaData
    (preStmt, (1 to meta.getColumnCount).map(meta.getColumnName(_)))
  }
}
