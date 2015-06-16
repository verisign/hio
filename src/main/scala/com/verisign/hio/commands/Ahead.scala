package com.verisign.hio.commands

import java.io.PrintStream

import com.typesafe.config.Config
import com.verisign.hadoopio.avro.AvroFsReader
import org.apache.avro.generic.GenericRecord
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hdfs.HdfsConfiguration
import org.rogach.scallop.ScallopConf

/**
 * The equivalent of Unix `head` for Avro files stored in HDFS and other supported file systems.
 */
class Ahead(paths: Seq[Path], hadoopConf: HdfsConfiguration, appConf: Config, out: PrintStream = Console.out) {

  def writeToConsole(n: Int): Unit = {
    val pathsWithItr = read(paths, n)
    pathsWithItr.size match {
      case 0 =>
      case 1 => pathsWithItr.head._2.foreach { out.println }
      case _ =>
        var firstPath = true
        val lines = (for {
          (path, records) <- pathsWithItr
          headerSep = if (firstPath) { firstPath = false; Seq.empty[String].toIterator } else Seq("").toIterator
          headerFileName = Seq(s"==> $path <==").toIterator
          first = false
          linesPerFile = headerSep ++ headerFileName ++ records map { _.toString }
        } yield linesPerFile).foldLeft(Iterator[String]())(_ ++ _)
        lines foreach out.println
    }
  }

  private def read(paths: Seq[Path], n: Int): Seq[(Path, Iterator[GenericRecord])] = {
    val itrs = paths.map { path =>
      val reader = AvroFsReader(hadoopConf)
      reader.read(path).take(n)
    }
    paths.zip(itrs)
  }

}

object Ahead {

  def apply(restArgs: Array[String], hadoopConf: HdfsConfiguration, appConf: Config): Unit = {
    val scallopConf = new AheadConf(restArgs, appConf)
    val numRecords = scallopConf.numRecords()
    val tool = {
      val paths = scallopConf.hdfsPath().map(new Path(_))
      new Ahead(paths, hadoopConf, appConf)
    }
    val count = if (numRecords <= 0) appConf.getInt("hio.commands.ahead.options.num-records") else numRecords
    tool.writeToConsole(count)
  }

}

private class AheadConf(args: Seq[String], appConf: Config) extends ScallopConf(args) {

  private lazy val cmdName = appConf.getString("hio.commands.ahead.name")
  private lazy val cmdSummary = appConf.getString("hio.commands.ahead.summary")

  banner(s"$cmdName -- $cmdSummary\nUsage: $cmdName [OPTION] <paths>\nOptions:\n")
  lazy val hdfsPath = trailArg[List[String]]("paths",
    descr = "HDFS paths (files or directories) from which Avro data will be read.  No recursion of directories.",
    required = true)
  lazy val numRecords = opt("num-records",
    descr = "How many records to dump.  Must be >= 1.",
    short = 'n',
    default = Option(0))
  lazy val help = opt[Boolean]("help", descr = "Show this help message", noshort = true)

  override def onError(e: Throwable) = Utils.exitWithError(e, printHelp, super.onError)

}