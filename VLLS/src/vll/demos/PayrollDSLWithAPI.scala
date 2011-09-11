/*
    Copyright 2010, Sanjay Dasgupta
    sanjay.dasgupta@gmail.com

    This file is part of VisualLangLab (http://vll.java.net/).

    VisualLangLab is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    VisualLangLab is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with VisualLangLab.  If not, see <http://www.gnu.org/licenses/>.
*/

package vll.demos

import java.io.File
import scala.util.parsing.input.CharSequenceReader
import vll.core.VllParsers

object PayrollDSLWithAPI {
  
  def main(args: Array[String]) {
  val input1 = """paycheck for employee "Buck Trends"
                 |is salary for 2 weeks minus deductions for {
                 |  federal income tax is 25. percent of gross,
                 |  state income tax is 5. percent of gross,
                 |  insurance premiums are 900. in gross currency,
                 |  retirement fund contributions are 10. percent of gross
                 |}""".stripMargin
    val vll = VllParsers.fromFile(new File("./grammars/payroll-parser-comb-pure.vll"))
println("vll: " + vll)
    val phraseParser = vll.phrase(vll.getParserFor("Paycheck"))
println("phraseParser: " + phraseParser)
    val parseResult = phraseParser(new CharSequenceReader(input1))
println("parseResult: " + phraseParser)
    parseResult match {
      case vll.Success(resultAST, _) => println()
      case vll.Failure(msg, where) => printf("Error: '%s' at line %d col %d%n", msg, where.pos.line, where.pos.column)
    }
  }
}
