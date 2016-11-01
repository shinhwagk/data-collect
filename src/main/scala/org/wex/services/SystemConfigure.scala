package org.wex.services

import java.io.{File, FileOutputStream}
import java.lang.management.ManagementFactory

/**
  * Created by zhangxu on 2016/9/2.
  */
object SystemConfigure {

  val cpus = Runtime.getRuntime().availableProcessors()

  def generatePid() {
    val pidOpt = ManagementFactory.getRuntimeMXBean.getName.split('@').headOption
    val pid = pidOpt getOrElse (throw new Exception("Couldn't determine current process's pid"))
    val pidFilePath = "RUNNING_PID"
    val pidFile = new File(pidFilePath).getAbsoluteFile
    if (pidFile.exists) {
      throw new Exception(s"This application is already running (Or delete ${pidFile.getPath} file).")
    }
    val out = new FileOutputStream(pidFile)
    try out.write(pid.getBytes) finally out.close()
  }
}
