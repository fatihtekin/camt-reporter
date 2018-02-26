package com.new10.camt
import com.new10.camt.CommandLineRunner.Config
import com.typesafe.scalalogging.LazyLogging
import generated.Document

import scala.util.{Failure, Success, Try}
import scala.xml.XML

/**
  * Reporter object
  */
object CamtReporter extends LazyLogging {

  /**
    * Reports camt file
    * @param config given to generate the report
    * @return list of string with report lines or error info. List is returned mostly used for testing purposes
    * Could have used a configurable file for the report template for now I have chosen to use logger as I already have to log them
    */
  def reportCamt(config: Config): List[String] = {
    Try(CamtService.accumulateMonthlyFinancials(scalaxb.fromXML[Document](XML.loadFile(config.camtFile)))) match {
      case Success(reports) =>
        logger.info("MM-YYYY|         NET_BALANCE")
        logger.info("-------|--------------------")
        val reportLines = reports.map(report => {
          val monthReportLine: String = f"${report.period.month}%2s-${report.period.year}|${report.balance}%16s ${report.currency}"
          logger.info(monthReportLine)
          monthReportLine
        })
        val dayInDebtForRecentMonths = reports.takeRight(config.mostRecentMonthCount).map(_.inDebtDays).sum
        val dayInDebtForRecentMonthsReportLine = s"Days in debt during the ${config.mostRecentMonthCount} most recent months is $dayInDebtForRecentMonths"
        logger.info(dayInDebtForRecentMonthsReportLine)
        reportLines ++ List(dayInDebtForRecentMonthsReportLine)
      case Failure(ex) =>
        val reportErrorLine: String = s"Error during parsing xml file content: ${ex.getMessage}"
        logger.error(reportErrorLine)
        List(reportErrorLine)
    }
  }
}
