package com.new10.camt
import com.new10.camt.CommandLineRunner.{Config, args, parser}
import com.typesafe.scalalogging.LazyLogging
import org.scalatest._

class CamtServiceTest extends WordSpec
    with BeforeAndAfterAll
    with LazyLogging
    with Matchers {

  "CAMTDocumentParser is called" when {

    "Valid list of  given" should {
      "Expect output list have the right data" in {
        CamtReporter.reportCamt(Config("src/test/resources/valid_camt.xml", 3)) shouldEqual List(
           " 9-2016|         -100.00 EUR",
           "10-2016|         -101.00 EUR",
           "11-2016|          -99.00 EUR",
           "12-2016|          901.11 EUR",
           "Days in debt during the 3 most recent months is 63"
        )
      }
    }
    "Valid report given" should {
      "Expect output list have the right data 0 days in debt for last n months" in {
        CamtReporter.reportCamt(Config("src/test/resources/valid_camt_0_daysindebt.xml", 3)) shouldEqual List(
          " 9-2016|         1000.00 EUR",
          "10-2016|          999.00 EUR",
          "11-2016|         1001.00 EUR",
          "12-2016|         2001.11 EUR",
          "Days in debt during the 3 most recent months is 0"
        )
      }
    }
    "Valid report given" should {
      "Expect output list have the right data full days in debt for last n months" in {
        CamtReporter.reportCamt(Config("src/test/resources/valid_camt_full_daysindebt.xml", 3)) shouldEqual List(
          " 9-2016|       -10000.00 EUR",
          "10-2016|       -10001.00 EUR",
          "11-2016|        -9999.00 EUR",
          "12-2016|        -8998.89 EUR",
          "Days in debt during the 3 most recent months is 92"
        )
      }
    }
    "Invalid report given" should {
      "Expect output list have the error report for invalid xml" in {
        CamtReporter.reportCamt(Config("src/test/resources/invalid_camt.xml", 3)) shouldEqual List(
          "Error during parsing xml file content: /Document/BkToCstmrStmt/Stmt/Bal must have only 2 entry as beginning and end"
        )
      }
    }
    "Invalid report given" should {
      "Expect output list have the error report for missing file" in {
        CamtReporter.reportCamt(Config("src/test/resources/missing_camt.xml", 3)) shouldEqual List(
          "Error during parsing xml file content: src\\test\\resources\\missing_camt.xml (The system cannot find the file specified)"
        )
      }
    }
    "Valid file is given with valid commandline parameters" should {
      "Expect report is generated without exception" in {
        CommandLineRunner.main(Array("-c", "3", "-f", "src/test/resources/valid_camt.xml"))
      }
    }



  }


}
