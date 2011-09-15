package vll.demos

import java.io.File
import vll.core.VllParsers

object ApiDemo {
  def main(args: Array[String]) {
  val payDSL = """paycheck for employee "Buck Trends"
          |is salary for 2 weeks minus deductions for {
          |  federal income tax is 25. percent of gross,
          |  state income tax is 5. percent of gross,
          |  insurance premiums are 900. in gross currency,
          |  retirement fund contributions are 10. percent of gross
          |}""".stripMargin
    val vllParsers = VllParsers.fromFile(new File("payroll-parser-comb-pure.vll"))
    val parser = vllParsers.getParserFor("Paycheck")
    vllParsers.parseAll(parser, payDSL) match {
        case vllParsers.Success(Array("\"Buck Trends\"", 
            Array("2", Pair(0, "weeks")), List(Pair(0, "25."), Pair(0, "5."), 
            Pair(1, "900."), Pair(0, "10."))), _) => println("Ok !")
        case vllParsers.NoSuccess(msg, _) => println("Error: + msg")
      }  
    vllParsers.parseAll(parser, "weekly") match {
        case vllParsers.Success(_, _) => println("Ok !")
        case vllParsers.NoSuccess(msg, _) => println("Error: " + msg)
      }  
    }
}
