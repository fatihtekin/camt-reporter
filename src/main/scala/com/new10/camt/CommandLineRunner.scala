package com.new10.camt

import com.typesafe.scalalogging.LazyLogging
import scopt.OptionParser

/**
  * Main method entry for the camt reporter tool
  */
object CommandLineRunner extends LazyLogging with App {

  //Commandline params to the case class
  case class Config(camtFile: String = null, mostRecentMonthCount: Int = -1)

  /**
    * Command line options to Config with validations
    */
  val parser: OptionParser[Config] = new scopt.OptionParser[Config]("CAMT") {
    head("CAMT Reporter")

    opt[String]('f', "camtfile").validate(x => if (new java.io.File(x).exists) success else failure(s"$x does not exists"))
      .required() action { (x, c) => c.copy(camtFile = x) } text "The canonical name of the camt file to report"

    opt[Int]('c', "mostRecentMonthCount").validate(x => if (x > 0) success else failure("Value <mostRecentMonthCount> must be >0"))
      .required() action { (x, c) => c.copy(mostRecentMonthCount = x) } text "The most recent months count reporting total in debt days in that month"
  }
  //Start the reporting
  parser.parse(args, Config()).map(CamtReporter.reportCamt)

}
