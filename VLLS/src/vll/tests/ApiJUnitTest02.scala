package vll.tests

import java.io.File
import org.junit.Before
import org.junit.After
import org.junit.Test
import org.junit.Assert._
import org.junit.runner.JUnitCore
import scala.util.parsing.input.CharSequenceReader
import vll.core.VllParsers
import scala.collection.JavaConversions._

class ApiJUnitTest02 {
  var parser: VllParsers#Parser[_] = null
  def parse(s: String) = {
    val pr = parser(new CharSequenceReader(s))
    if (pr.successful)
      pr.get
    else
      throw new Exception("Parser failure")
  }
  
  @Before def setUp() {
    println("setup entry")
    val vll = VllParsers.fromFile(new File("./ArithExpr2.vll"))
    parser = vll.phrase(vll.getParserFor("Expr"))
    println("setup exit")
  }
  
  @Test def addition() {
    assertTrue(parse("33 + 55") == 88)
  }
  @Test def multiplication() {
    assertTrue(parse("11 * 12") == 132)
  }
  @Test def division() {
    assertTrue(parse("132 / 11") == 12)
  }
  @Test def subtraction() {
    assertTrue(parse("63 - 21") == 42)
  }
}

object ApiJUnitTest02 {
  def main(args: Array[String]) {
    val res = JUnitCore.runClasses(classOf[ApiJUnitTest02])
    println(res.getFailureCount)
    res.getFailures.foreach(println)
  }
}
