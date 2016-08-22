package org.wex.services

import java.sql.{Connection, DriverManager}
import java.time.{LocalDate, LocalTime}

/**
  * Created by zhangxu on 2016/8/15.
  */
object CommonServices {
  def createOracleConnection(ds: DBSource): Connection =
    DriverManager.getConnection(s"jdbc:oracle:thin:@${ds.jdbc}", ds.user, ds.password);

  def closeOracleConnection(conn: Connection): Unit = conn.close()

  def timerFilter(timer: String): Boolean = {
    val date = LocalDate.now()
    val time = LocalTime.now()

    implicit def preZero(n: Int): String = f"$n%02d"

    val minute: String = time.getMinute
    val hour: String = time.getHour
    val day: String = date.getDayOfMonth
    val month: String = date.getMonthValue
    val year: String = date.getYear

    val matchMinute: String = List("0000", "00", "00", "00", minute).mkString("-")
    val matchHour: String = List("0000", "00", "00", hour, minute).mkString("-")
    val matchDayOfMonth: String = List("0000", "00", month, hour, minute).mkString("-")
    val matchMonth: String = List("0000", month, month, hour, minute).mkString("-")
    val matchYear: String = List(year, month, month, hour, minute).mkString("-")

    timer match {
      case s: String if s == matchMinute => true
      case s: String if s == matchHour => true
      case s: String if s == matchDayOfMonth => true
      case s: String if s == matchMonth => true
      case s: String if s == matchYear => true
      case _ => false
    }

  }
}
