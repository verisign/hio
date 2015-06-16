organization := "com.verisign.hio"

name := "hio"

homepage := Some(url("https://github.com/verisign/hio"))

licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html"))

description := "Command line tools to interact with Hadoop HDFS and other supported file systems"

scalaVersion := "2.10.4"

// https://github.com/jrudolph/sbt-dependency-graph
net.virtualvoid.sbt.graph.Plugin.graphSettings

resolvers ++= Seq(
  "typesafe-repository" at "https://repo.typesafe.com/typesafe/releases/",
  // http://www.cloudera.com/content/cloudera/en/documentation/cdh4/latest/CDH4-Installation-Guide/cdh4ig_using_Maven.html
  "cloudera-repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/"
  // TODO: Add repository for https://github.com/verisign/hadoopio once it is released (planned: Maven Central).
)

// -------------------------------------------------------------------------------------------------------------------
// Variables
// -------------------------------------------------------------------------------------------------------------------

val avroVersion = "1.7.7"
// See http://www.cloudera.com/content/cloudera/en/documentation/core/v5-2-x/topics/cdh_vd_cdh5_maven_repo.html
val hadoopVersion = "2.5.0-cdh5.2.1"
val hadoopMapReduceVersion = "2.5.0-mr1-cdh5.2.1"
val javaVersion = sys.env.get("SBT_JAVA_VERSION").getOrElse("1.7")
val logbackVersion = "1.1.2"


// -------------------------------------------------------------------------------------------------------------------
// Dependencies
// -------------------------------------------------------------------------------------------------------------------

// Main dependencies
libraryDependencies ++= Seq(
  "com.verisign.hadoopio" %% "hadoopio" % "0.2.0-SNAPSHOT",
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion,
  // Contains e.g. compression codecs
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion
    exclude("org.slf4j", "slf4j-log4j12"),
  "org.rogach" %% "scallop" % "0.9.5",
  "com.typesafe" % "config" % "1.2.1",
  // Logback with slf4j facade
  "ch.qos.logback" % "logback-classic" % logbackVersion
)

// Required IntelliJ workaround.  This tells `sbt gen-idea` to include scala-reflect as a compile dependency (and not
// merely as a test dependency), which we need for TypeTag usage.
libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _)

// Test dependencies
libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "18.0" % "test",
  // The 'classifier "tests"' modifier is required to pull in the hadoop-hdfs tests jar, which contains MiniDFSCluster.
  "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion % "test" classifier "tests",
  // Required for e.g. org.apache.hadoop.net.StaticMapping
  "org.apache.hadoop" % "hadoop-common" % hadoopVersion % "test" classifier "tests",
  "org.apache.hadoop" % "hadoop-minicluster" % hadoopMapReduceVersion % "test",
  "org.scalatest" %% "scalatest" % "2.2.2" % "test"
)


// -------------------------------------------------------------------------------------------------------------------
// Compiler and JVM options
// -------------------------------------------------------------------------------------------------------------------

// Enable forking (see sbt docs) because our full build (including tests) uses many threads.
fork := true

// The following options are passed to forked JVMs.
//
// Note: If you need to pass options to the JVM used by sbt (i.e. the "parent" JVM), then you should modify `.sbtopts`.
javaOptions ++= Seq(
  "-Xmx256m",
  "-XX:+UseG1GC",
  "-Djava.awt.headless=true",
  "-Djava.net.preferIPv4Stack=true")

javacOptions in Compile ++= Seq(
  "-source", javaVersion,
  "-target", javaVersion,
  "-Xlint:deprecation")

scalacOptions ++= Seq(
  "-target:jvm-" + javaVersion,
  "-encoding", "UTF-8"
)

scalacOptions in Compile ++= Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs.
  "-feature",  // Emit warning and location for usages of features that should be imported explicitly.
  "-unchecked", // Enable additional warnings where generated code depends on assumptions.
  "-Xlint", // Enable recommended additional warnings.
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver.
  "-Ywarn-dead-code",
  "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
)

scalacOptions in Test ~= { (options: Seq[String]) =>
  options.filterNot(_ == "-Ywarn-value-discard").filterNot(_ == "-Ywarn-dead-code" /* to fix warnings due to Mockito */)
}

scalacOptions in ScoverageTest ~= { (options: Seq[String]) =>
  options.filterNot(_ == "-Ywarn-value-discard").filterNot(_ == "-Ywarn-dead-code" /* to fix warnings due to Mockito */)
}


// -------------------------------------------------------------------------------------------------------------------
// Testing
// -------------------------------------------------------------------------------------------------------------------

parallelExecution in ThisBuild := false

// Write test results to file in JUnit XML format
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-u", "target/test-reports/junitxml")

// Write test results to console.
//
// Tip: If you need to troubleshoot test runs, it helps to use the following reporting setup for ScalaTest.
//      Notably these suggested settings will ensure that all test output is written sequentially so that it is easier
//      to understand sequences of events, particularly cause and effect.
//      (cf. http://www.scalatest.org/user_guide/using_the_runner, section "Configuring reporters")
//
//        testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oUDT", "-eUDT")
//
//        // This variant also disables ANSI color output in the terminal, which is helpful if you want to capture the
//        // test output to file and then run grep/awk/sed/etc. on it.
//        testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oWUDT", "-eWUDT")
//
testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-o")

// https://github.com/scoverage/scalac-scoverage-plugin
instrumentSettings


// ---------------------------------------------------------------------------------------------------------------
// Scaladoc settings
// ---------------------------------------------------------------------------------------------------------------
(scalacOptions in doc) <++= (name, version).map { (n, v) => Seq("-doc-title", n, "-doc-version", v) }

// https://github.com/sbt/sbt-unidoc
unidocSettings


// -------------------------------------------------------------------------------------------------------------------
// Packaging
// -------------------------------------------------------------------------------------------------------------------
//
// We use http://www.scala-sbt.org/sbt-native-packager/ to create RPMs and such.
//
enablePlugins(JavaAppPackaging)

enablePlugins(LinuxPlugin)

enablePlugins(RpmPlugin)

packageName := "hio"

val hioMaintainer = "Verisign"

maintainer := hioMaintainer

packageSummary := "Command line utilities to interact with Hadoop HDFS"

packageDescription := "Unix-like tools (think: cat, head) to interact with Hadoop HDFS and other supported file systems"

rpmVendor := hioMaintainer

rpmUrl := Some("https://github.com/verisign/hio")

// See:
// http://fedoraproject.org/wiki/Packaging:NamingGuidelines
// http://stackoverflow.com/questions/24191469
rpmRelease <<= (version) { v =>
  val branch = "git rev-parse --abbrev-ref HEAD".!!.trim
  val commit = "git rev-parse HEAD".!!.trim
  val shortCommit = commit.substring(0, 7)
  val buildTime = (new java.text.SimpleDateFormat("yyyyMMdd")).format(new java.util.Date())
  val defaultBuildNumber = "1"
  val releaseNumberIncrement = sys.env.get("BUILD_NUMBER").getOrElse(defaultBuildNumber)
  val snapshotReleaseTag = "0.%s.%sgit%s".format(releaseNumberIncrement, buildTime, shortCommit)
  val defaultReleaseNumber = "1"
  if (v.trim.endsWith("SNAPSHOT")) snapshotReleaseTag else defaultReleaseNumber
}

// Dashes ("-") are not allowed in RPM version strings.
//
// Alternatively you can use the following sbt syntax:
//
//      version in Rpm := {
//        val v = version.value
//        if (v.trim.endsWith("SNAPSHOT")) v.trim.replace("-", "_") else v
//      }
//
version in Rpm <<= (version) { v => if (v.trim.endsWith("SNAPSHOT")) v.trim.replace("-SNAPSHOT", "").replace("-", "_") else v }

// See http://fedoraproject.org/wiki/RPMGroups
// Examples: Run `rpm -qa --qf '%{group}\n' | sort -u` on a RHEL/CentOS box.
rpmGroup := Some("Development/Tools")

rpmLicense := Some("Copyright (c) 2015 Verisign. All rights reserved.")

// Required fix for the following error when building RPMs on Mac OS X:
//
//    [error] + '%{_rpmconfigdir}/brp-compress'
//    [error] /var/folders/p8/5f6wty7x3gsc75jy5z7wq8jcs0nrvl/T/sbt_9afa0c1e/rpm-tmp.97268: line 32: fg: no job control
//    [error] error: Bad exit status from /var/folders/p8/5f6wty7x3gsc75jy5z7wq8jcs0nrvl/T/sbt_9afa0c1e/rpm-tmp.97268 (%install)
//
// Note: On RHEL/CentOS the `_rpmconfigdir` RPM macro is defined in `/usr/lib/rpm/macros`, and the `brp-compress` tool
// is included in the `rpm-build` package.
//
// See https://github.com/sbt/sbt-native-packager/issues/266 for the cause and workaround
// (additional information at https://github.com/Homebrew/homebrew/issues/35062)
// and http://www.scala-sbt.org/sbt-native-packager/formats/rpm.html#jar-repackaging for what this option does.
rpmBrpJavaRepackJars := true

// Required by the native packager plugin so that it knows which class to run.
mainClass in (Compile, run) := Some("com.verisign.pe.hio.Main")

// Customize the generated bin script
bashScriptExtraDefines += """addJava "-Dconfig.file=${app_home}/../conf/hio.conf""""


// -------------------------------------------------------------------------------------------------------------------
// Releasing
// -------------------------------------------------------------------------------------------------------------------
publishMavenStyle := true

publishArtifact in Test := false

// TODO: Add integration with SonaType to publish to Maven Central under `com.verisign`.

pomIncludeRepository := { _ => false }

pomExtra :=
  <scm>
    <url>https://github.com/verisign/hio/</url>
    <connection>scm:git:git@github.com:verisign/hio.git</connection>
  </scm>
  <developers>
    <developer>
      <id>mnoll</id>
      <name>Michael G. Noll</name>
      <email>mnoll@verisign.com</email>
      <timezone>Europe/Zurich</timezone>
    </developer>
  </developers>
