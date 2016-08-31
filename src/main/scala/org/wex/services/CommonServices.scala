package org.wex.services

import java.sql.{Connection, DriverManager}
import java.time.{LocalDate, LocalTime}

/**
  * Created by zhangxu on 2016/8/15.
  */
object CommonServices {
  def createOracleConnection(ds: DBSource): Connection =
    DriverManager.getConnection(s"jdbc:oracle:thin:@${ds.jdbc}", ds.user, ds.password);

  def timerFilter(timer: String): Boolean = {
    val date = LocalDate.now()
    val time = LocalTime.now()

    implicit def preZero(n: Int): String = f"$n%02d"

    val minute = time.getMinute
    val hour = time.getHour
    val day = date.getDayOfMonth
    val month = date.getMonthValue
    val year = date.getYear

    val matchMinute = List[String]("0000", "00", "00", "00", minute).mkString("-")
    val matchHour = List[String]("0000", "00", "00", hour, minute).mkString("-")
    val matchDayOfMonth = List[String]("0000", "00", month, hour, minute).mkString("-")
    val matchMonth = List[String]("0000", month, month, hour, minute).mkString("-")
    val matchYear = List[String](year, month, month, hour, minute).mkString("-")

    List(matchMinute, matchHour, matchDayOfMonth, matchMonth, matchYear)
      .find(_ == timer)
      .map(_ => true)
      .getOrElse(false)
  }
}
