package vll.tests

import java.io.File
import org.junit.Test
import org.junit.Assert._
import org.junit.runner.JUnitCore
import vll.core.VllParsers
import scala.collection.JavaConversions._

class PayrollDSLTest {
  val withDeductions = """paycheck for employee "Buck Trends"
          |is salary for 2 weeks minus deductions for {
          |  federal income tax is 25. percent of gross,
          |  state income tax is 5. percent of gross,
          |  insurance premiums are 900. in gross currency,
          |  retirement fund contributions are 10. percent of gross
          |}""".stripMargin
  val noDeductions = """paycheck for employee "Buck Trends"
          |is salary for 2 weeks minus deductions for {
          |}""".stripMargin
  
  val vllParsers = VllParsers.fromFile(new File("payroll-parser-comb-noaction.vll"))
  
  @Test def weekDays() {
    val parser = vllParsers.getParserFor("weekDays")
    // Positive tests ...
    assertTrue(vllParsers.parseAll(parser, "week") match 
        {case vllParsers.Success(Pair(0, "week"), _) => true; case _ => false})
    assertTrue(vllParsers.parseAll(parser, "weeks") match 
        {case vllParsers.Success(Pair(0, "weeks"), _) => true; case _ => false})
    assertTrue(vllParsers.parseAll(parser, "day") match 
        {case vllParsers.Success(Pair(1, "day"), _) => true; case _ => false})
    assertTrue(vllParsers.parseAll(parser, "days") match 
        {case vllParsers.Success(Pair(1, "days"), _) => true; case _ => false})
    // Negative tests ...
    assertFalse(vllParsers.parseAll(parser, "daily") match 
        {case vllParsers.Success(_, _) => true; case _ => false})
    assertFalse(vllParsers.parseAll(parser, "weekly") match 
        {case vllParsers.Success(_, _) => true; case _ => false})
  }
  
  @Test def paycheck() {
    val parser = vllParsers.getParserFor("Paycheck")
    assertTrue(vllParsers.parseAll(parser, noDeductions) match {
        case vllParsers.Success(Array("\"Buck Trends\"", 
            Array("2", Pair(0, "weeks")), Nil), _) => true
        case _ => false})
    assertTrue(vllParsers.parseAll(parser, withDeductions) match {
        case vllParsers.Success(Array("\"Buck Trends\"", Array("2", Pair(0, "weeks")), 
            List(Pair(0, "25."), Pair(0, "5."), Pair(1, "900."), 
            Pair(0, "10."))), _) => true
        case _ => false})
  }
}

object PayrollDSLTest {
  def main(args: Array[String]) {
    val res = JUnitCore.runClasses(classOf[PayrollDSLTest])
    println(res.getFailureCount)
    res.getFailures.foreach(println)
  }
}
