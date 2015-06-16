package com.verisign.hio.commands

import org.rogach.scallop.exceptions.ScallopException

object Utils {

  def exitWithError(e: Throwable, f: () => Unit, fallback: Throwable => Unit, exitCode: Int = 2) = e match {
    case ScallopException(message) =>
      Console.err.println("ERROR: " + message + "\n")
      f()
      System.exit(exitCode)
    case ex => fallback(ex)
  }

}
