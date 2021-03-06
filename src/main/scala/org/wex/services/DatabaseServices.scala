package org.wex.services

import java.sql.{Connection, DriverManager, ResultSet, Statement}
import org.wex.services.Config.DBSource

/**
  * Created by zhangxu on 2016/8/22.
  */
object DatabaseServices {

  Class.forName("oracle.jdbc.driver.OracleDriver");

  def generateQueryIterator(rs: ResultSet, cols: Seq[String]): Iterator[Seq[(String, Object)]] = {
    new Iterator[Seq[(String, Object)]] {
      override def hasNext: Boolean = rs.next()

      override def next(): Seq[(String, Object)] = cols.map(p => (p, rs.getObject(p)))
    }
  }

  def closeConnection(conn: Connection): Unit = conn.close()

  def insertSqlTemplate(columns: Seq[String], table: String): String =
    s"INSERT INTO ${table}(${columns.mkString(",")}) VALUES " + (1 to columns.size).map(_ => "?").mkString("(", ",", ")")

  def getColumnListBySql(conn: Connection, sql: String): (Statement, Seq[String]) = {
    val preStmt = conn.prepareStatement(sql)
    val meta = preStmt.getMetaData
    (preStmt, (1 to meta.getColumnCount).map(meta.getColumnName(_)))
  }

  def createConnection(ds: DBSource): Connection = DriverManager.getConnection(ds.jdbc, ds.user, ds.password)

}
