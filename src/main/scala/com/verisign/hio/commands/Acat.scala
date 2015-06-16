package com.verisign.hio.commands

import java.io.PrintStream

import com.typesafe.config.Config
import com.verisign.hadoopio.avro.AvroFsReader
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hdfs.HdfsConfiguration
import org.rogach.scallop.ScallopConf

/**
 * The equivalent of Unix `cat` for Avro files stored in HDFS and other supported file systems.
 */
class Acat(paths: Seq[Path], hadoopConf: HdfsConfiguration, appConf: Config, out: PrintStream = Console.out) {

  def writeToConsole(): Unit = read(paths) foreach out.println

  private def read(paths: Seq[Path]): Iterator[GenericRecord] = paths.map { path =>
    val reader = AvroFsReader(hadoopConf)
    reader.read(path)
  }.foldLeft(Iterator[GenericRecord]())(_ ++ _)

}

object Acat {

  def apply(restArgs: Array[String], hadoopConf: HdfsConfiguration, appConf: Config): Unit = {
    val scallopConf = new AcatConf(restArgs, appConf)
    val tool = {
      val paths = scallopConf.hdfsPath().map(new Path(_))
      new Acat(paths, hadoopConf, appConf)
    }
    tool.writeToConsole()
  }

}

private class AcatConf(args: Seq[String], appConf: Config) extends ScallopConf(args) {

  private lazy val cmdName = appConf.getString("hio.commands.acat.name")
  private lazy val cmdSummary = appConf.getString("hio.commands.acat.summary")

  banner(s"$cmdName -- $cmdSummary\nUsage: $cmdName [OPTION] <paths>\nOptions:\n")
  lazy val hdfsPath = trailArg[List[String]]("paths",
    descr = "HDFS paths (files or directories) from which Avro data will be read.  No recursion of directories.",
    required = true)
  lazy val help = opt[Boolean]("help", descr = "Show this help message", noshort = true)

  override def onError(e: Throwable) = Utils.exitWithError(e, printHelp, super.onError)

}