# hio

Command line utilities to interact with Hadoop HDFS.  Think: Hadoop I/O CLI.

"hio" is a shorthand for Hadoop I/O because less typing on the CLI is better!
It is also a shout-out to the [hadoopio](https://github.com/verisign/hadoopio) library, which we use behind the
scenes.


---

Table of Contents

* <a href="#philosophy">Philosophy</a>
* <a href="#usage">Usage</a>
    * <a href="#overview">Overview</a>
    * <a href="#examples">Examples</a>
* <a href="#deployment">Deployment</a>
    * <a href="#requirements">Run-time requirements</a>
    * <a href="#installation">Installation</a>
    * <a href="#configuration">Configuration</a>
* <a href="#development">Development</a>
* <a href="#todo">Todo</a>
* <a href="#changelog">Change log</a>
* <a href="#Contributing">Contributing</a>
* <a href="#Authors">Authors</a>
* <a href="#License">License</a>
* <a href="#References">References</a>

---


<a name="philosophy"></a>

# Philosophy and how it works

We strive to mimic existing Unix CLI tools as much as possible, e.g. in terms of names and semantics of hio commands
and their respective parameters.  That being said, in some cases we may not be able to achieve this goal due to
technical reasons or upstream limitations (e.g. HDFS).

Hio is based on [hadoopio](https://github.com/verisign/hadoopio), which means hio commands typically require only
constant memory, i.e. even if you process 1 TB of Avro data your machine should basically never run out of memory.


<a name="usage"></a>

# Usage


<a name="overview"></a>

## Overview

Once installed the main entry point is the `hio` executable:

```bash
$ hio
hio: Command line tools to interact with Hadoop HDFS and other supported file systems

Commands:
  acat       - concatenates and prints Avro files
  ahead      - displays the first records of an Avro file
```

**Tip:** You can tweak many Java/JVM related settings via `hio` CLI options.  See `hio -h` for details.


<a name="examples"></a>

## Examples

```bash
# Show the first two lines of the HDFS files `tweets1.avro` and `tweets2.avro`.
$ hio ahead -n 2 /hdfs/path/to/tweets/tweets1.avro /hdfs/path/to/tweets/tweets2.avro
==> /hdfs/path/to/tweets/tweets1.avro <==
{"username":"miguno","tweet":"Rock: Nerf paper, scissors is fine.","timestamp": 1366150681 }
{"username":"BlizzardCS","tweet":"Works as intended.  Terran is IMBA.","timestamp": 1366154481 }

==> /hdfs/path/to/tweets/tweets2.avro <==
{"username":"Zergling","tweet":"Cthulhu R'lyeh!","timestamp": 1366154399 }
{"username":"miguno","tweet":"4-Gate is the new 6-Pool.","timestamp": 1366150900 }
```

**Tip:** Use [jq](http://stedolan.github.io/jq/) to pretty-print, color, or otherwise post-process the JSON output.

```bash
# Extract only the `username` field from the JSON output.
$ hio ahead -2 /hdfs/path/to/tweets/tweets1.avro | jq '.username'
"miguno"
"BlizzardCS"

# Don't like the escape quotes?  Use `--raw-output` aka `-r`.
$ hio ahead -2 /hdfs/path/to/tweets/tweets1.avro | jq --raw-output '.username'
miguno
BlizzardCS

# Let's extract the `username` and `timestamp` fields, and separate them with tabs.
$ hio ahead -2 /hdfs/path/to/tweets/tweets1.avro | jq --raw-output '"\(.username)\t\(.timestamp)"'
miguno  1366150681
BlizzardCS      1366154481
```


<a name="deployment"></a>

# Deployment


<a name="requirements"></a>

## Run-time requirements

* Java 7, preferably Oracle JRE/JDK 1.7
* RHEL/CentOS (required only because we package exclusively in RPM format at the moment)
* A "compatible" HDFS cluster running Hadoop 2.x.  See below for details.

**A note on Hadoop versions:**
We bundle all required libraries (read: jar files) when packaging hio, which means that hio does not require any
additional software packages or libraries.  Be aware though that you will need to keep the libraries used by hio in
sync with the Hadoop version of your cluster.

[build.sbt](build.sbt) lists the exact version of Hadoop that hio has been built against.
At the moment, we are targeting Cloudera CDH 5.x, which is essentially Hadoop 2.5.x.  Feel free to run hio against
other Hadoop versions or Hadoop distributions such as HortonWorks HDP, and report back the results.


<a name="installation"></a>

## Installation

You can package hio as an RPM (see section below) and then install the package via `yum`, `rpm`, or deployment tools
such as Puppet or Ansible.

> Non-RHEL users: If you need different packaging formats (say, `.deb` for Debian or Ubuntu), please let us know!


<a name="configuration"></a>

## Configuration

The most important configuration aspect is making hio aware of your Hadoop HDFS cluster.  Fortunately, in most cases
hio will "just work" out of the box because it follows Hadoop best practices.

If hio does not work automagically, then you must tell hio where to find your HDFS cluster.  Here, hio expects to find
the Hadoop configuration files -- typically named `core-site.xml` and `hdfs-site.xml` -- in the directory specified by
the standard `HADOOP_CONF_DIR` variable.  If this environment variable does not exist, hio will fall back to
`/etc/hadoop/conf/` (see [hio.conf](src/universal/conf/hio.conf), which is installed to `/usr/share/hio/conf/hio.conf`
by the RPM).

You can apply standard shell practices if you need to override the `HADOOP_CONF_DIR` variable for some reason:

```bash
HADOOP_CONF_DIR=/my/custom/hadoop/conf hio ...
```


<a name="development"></a>

# Development


## Build requirements

* Java 7, preferably Oracle JDK 1.7


## Building the code

    $ ./sbt compile


## Running the tests

Run the test suite:

    $ ./sbt test


## Packaging

_For details see [sbt-native-packager](http://www.scala-sbt.org/sbt-native-packager/)._

Create an RPM (preferred package format):

    $ ./sbt rpm:packageBin

    >>> Creates target/rpm/RPMS/noarch/hio-<VERSION>.noarch.rpm

Create a Tarball:

    $ ./sbt universal:packageZipTarball

    >>> Creates ./target/universal/hio-<VERSION>.tgz

Another helpful task is `stage`, which (quickly) e.g. generates the shell wrapper scripts but does not put them into an
RPM -- so they are easier to inspect while developing:

    $ ./sbt stage

    >>> Creates files under ./target/universal/stage/ (e.g. bin/, conf/, lib/)


<a name="todo"></a>

# TODO

* [ ] Support wildcards/globbing of Hadoop paths (cf. hadoopio's include patterns) so that we understand
      paths such as `/foo/bar*.avro`.


<a name="changelog"></a>

# Change log

See [CHANGELOG](CHANGELOG.md).


<a name="Contributing"></a>

# Contributing to this project

Code contributions, bug reports, feature requests etc. are all welcome.

If you are new to GitHub please read [Contributing to a project](https://help.github.com/articles/fork-a-repo) for how
to send patches and pull requests to this project.


<a name="Authors"></a>

# Authors

* [Michael Noll](https://github.com/miguno)
* [Kevin Mao](https://github.com/KevinJMao)


<a name="License"></a>

# License

Copyright © 2015 [VeriSign, Inc.](http://www.verisigninc.com/)

See [LICENSE](LICENSE) for licensing information.


<a name="References"></a>

# References

Alternative ways to parse CLI options for Hadoop-aware apps:

* [GenericOptionsParser](https://hadoop.apache.org/docs/r1.2.1/api/org/apache/hadoop/util/GenericOptionsParser.html)
  (for Hadoop 1.x only)
* [ToolRunner](https://hadoop.apache.org/docs/r2.6.0/api/org/apache/hadoop/util/ToolRunner.html)
  and [Tool](https://hadoop.apache.org/docs/r2.6.0/api/org/apache/hadoop/util/Tool.html) (for Hadoop 2.x)
