package com.new10.camt

import javax.xml.datatype.DatatypeFactory
import com.typesafe.scalalogging.LazyLogging
import generated.{Amt2, CRDT, DBIT, Document, Ntry, Stmt, ValDt}
import org.joda.time.DateTime

import scala.collection.immutable.ListMap
import scala.math.BigDecimal.RoundingMode

/**
  * Service to process Document record
  */
object CamtService extends LazyLogging {

  case class Period(month: Int, year: Int) extends Ordered[Period] {
    import scala.math.Ordered.orderingToOrdered
    //Helps to order the periods
    def compare(that: Period): Int = (this.year, this.month) compare(that.year, that.month)
  }

  implicit class toScale(bd: BigDecimal) {
    def scale2Fractions(): BigDecimal = bd.setScale(2, RoundingMode.HALF_UP)
  }

  /**
    * @param period month,year period
    * @param inDebtDays days passed in debt
    * @param balance balance at the end of the month
    * @param currency currency used
    */
  case class Report(period: Period, inDebtDays: Int, balance: BigDecimal, currency: String)

  /**
    * Does those in order,
    *   First get the balanceAtTheBeginning,
    *   Second group ntrys by periods(month,year) and sort by period
    *   Third group ntrys by days and sort by day
    *   Count the days in debt for the month and  wrap the data in Report case class
    * @param doc
    * @return
    */
  def accumulateMonthlyFinancials(doc: Document): List[Report] = {
    validateDocument(doc)
    val stmt = doc.BkToCstmrStmt.Stmt
    val balanceAtTheBeginning = stmt.Bal.minBy(_.Dt.Dt.toGregorianCalendar.getTimeInMillis)
    var balance: BigDecimal = balanceAtTheBeginning.CdtDbtInd match {
      case CRDT => balanceAtTheBeginning.Amt.value.scale2Fractions()
      case DBIT => -balanceAtTheBeginning.Amt.value.scale2Fractions()
    }
    ListMap((stmt.Ntry ++ getCompletingDays(stmt))
      .groupBy(ntry => Period(ntry.ValDt.Dt.getMonth, ntry.ValDt.Dt.getYear)).toSeq.sortBy { case (p, _) => (p.year, p.month) }: _*)
      .map { case (period, mothlyNtrys) =>
        var inDebtDays = 0
        mothlyNtrys.groupBy(_.ValDt.Dt.getDay).toSeq.sortBy{ case (day, _) => day}.foreach { case (_, ntrys) =>
          validateNtrys(ntrys)
          balance += calculateTotal(ntrys)
          if (balance < 0) inDebtDays += 1
        }
        Report(period, inDebtDays, balance, doc.BkToCstmrStmt.Stmt.Acct.Ccy)
      }.toList
  }

  /**
    * Prepares 0 balance ntrys records for all the days so that we wont miss the days to report
    * I found this logic easier to maintain as it quite straight forward to generate the total days
    */
  private def getCompletingDays(stmt: Stmt): Seq[Ntry] = {
    val factory = DatatypeFactory.newInstance()
    Iterator.iterate(new DateTime(stmt.FrToDt.FrDtTm.toGregorianCalendar.getTime)) {_.plusDays(1)}
      .takeWhile(_.isBefore(new DateTime(stmt.FrToDt.ToDtTm.toGregorianCalendar.getTime).plusDays(1)))
      .map(date => Ntry(null, Amt2(BigDecimal.apply(0.00)), CRDT, null, null, ValDt(factory.newXMLGregorianCalendar(date.toGregorianCalendar)), null))
      .toSeq
  }

  /**
    * Calculate the total value by sum(CRDT)-sum(DBIT) using ntrys list
    */
  private def calculateTotal(ntrys: Seq[Ntry]): BigDecimal = {
    ntrys.map(ntry => ntry.CdtDbtInd match {
      case CRDT => ntry.Amt.value.scale2Fractions()
      case DBIT => -ntry.Amt.value.scale2Fractions()
    }).sum
  }

  /**
    * Validate document
    */
  private def validateDocument(doc: Document): Unit = {
    if (doc.BkToCstmrStmt == null || doc.BkToCstmrStmt.Stmt == null) {
      throw new IllegalArgumentException(s"Document/BkToCstmrStmt/Stmt does not exist in the CAMT document")
    }
    val stmt = doc.BkToCstmrStmt.Stmt
    if (stmt.Acct == null) {
      throw new IllegalArgumentException(s"Document/BkToCstmrStmt/Stmt/Acct does not exist in the CAMT document")
    }
    val currency = stmt.Acct.Ccy
    if (currency == null || currency.isEmpty) {
      throw new IllegalArgumentException(s"Document/BkToCstmrStmt/Stmt/Acct/Ccy does not exist in the CAMT document")
    }
    stmt.Bal.foreach(bal =>
      if (bal.Amt == null || Option(bal.Amt.value).isEmpty || bal.Dt == null || bal.Dt.Dt == null) {
        throw new IllegalArgumentException("/Document/BkToCstmrStmt/Stmt/Bal has missing fields")
      }
    )
    if (stmt.Bal.length != 2) {
      throw new IllegalArgumentException("/Document/BkToCstmrStmt/Stmt/Bal must have only 2 entry as beginning and end")
    }
  }

  /**
    * Validate ntry
    * @param ntrys
    */
  private def validateNtrys(ntrys: Seq[Ntry]): Unit = {
    ntrys.foreach(ntry =>
      if (ntry.Amt == null || Option(ntry.Amt.value).isEmpty || ntry.ValDt == null || ntry.ValDt.Dt == null) {
        throw new IllegalArgumentException("/Document/BkToCstmrStmt/Stmt/Ntry has missing fields")
      }
    )
  }
}