package com.verisign.hio

import com.typesafe.config.ConfigFactory
import com.verisign.hio.commands.{Acat, Ahead}
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hdfs.HdfsConfiguration

import scala.language.reflectiveCalls

object Main {

  private val appConf = ConfigFactory.load()

  private val hadoopConf: HdfsConfiguration = {
    val hadoopConfDir =
      Option(System.getenv("HADOOP_CONF_DIR")).getOrElse(appConf.getString("hadoop.envs.hadoop-conf-dir"))
    val c = new HdfsConfiguration()
    c.addResource(new Path(hadoopConfDir, "core-default.xml"))
    c.addResource(new Path(hadoopConfDir, "core-site.xml"))
    c.addResource(new Path(hadoopConfDir, "hdfs-default.xml"))
    c.addResource(new Path(hadoopConfDir, "hdfs-site.xml"))
    c
  }

  def cmdNames = new {
    val acat = appConf.getString("hio.commands.acat.name")
    val ahead = appConf.getString("hio.commands.ahead.name")
  }

  def main(args: Array[String]) {
    val appConf = ConfigFactory.load()
    if (args != null && args.nonEmpty) {
      val cmd = args(0)
      val restArgs = args drop 1
      cmd match {
        case s if s == cmdNames.acat => Acat(restArgs, hadoopConf, appConf)
        case s if s == cmdNames.ahead => Ahead(restArgs, hadoopConf, appConf)
        case _ => printMainHelpAndExit()
      }
    }
    else {
      printMainHelpAndExit()
    }
  }

  private def printMainHelpAndExit(exitCode: Int = 1): Unit = {
    Console.err.println(s"${appConf.getString("hio.meta.name")}: ${appConf.getString("hio.meta.summary")}")
    Console.err.println()
    Console.err.println("Commands:")
    Console.err.println(f"  ${cmdNames.acat}%-10s -- ${appConf.getString("hio.commands.acat.summary")}")
    Console.err.println(f"  ${cmdNames.ahead}%-10s -- ${appConf.getString("hio.commands.ahead.summary")}")
    System.exit(exitCode)
  }

}