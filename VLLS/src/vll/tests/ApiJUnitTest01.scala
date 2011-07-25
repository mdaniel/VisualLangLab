package vll.tests

import java.io.File
import org.junit.Before
import org.junit.Test
import org.junit.Assert._
import org.junit.runner.JUnitCore
import scala.util.parsing.input.CharSequenceReader
import vll.core.VllParsers
import scala.collection.JavaConversions._

class ApiJUnitTest01 {
  def areSameAST(a: Any, b: Any): Boolean = (a, b) match {
    case (aa: Array[_], bb: Array[_]) => aa.length == bb.length && 
      aa.zip(bb).forall(p => areSameAST(p._1, p._2))
    case (aa: List[_], bb: List[_]) => aa.length == bb.length && 
      aa.zip(bb).forall(p => areSameAST(p._1, p._2))
    case (None, None) => true
    case (aa: Some[_], bb: Some[_]) => areSameAST(aa.get, bb.get)
    case (aa: Pair[_,_], bb: Pair[_,_]) => areSameAST(aa._1, bb._1) && areSameAST(aa._2, bb._2)
    case (_, _) => a == b
  }
  var parser: VllParsers#Parser[_] = null
  def parse(s: String) = {
    val pr = parser(new CharSequenceReader(s))
    if (pr.successful)
      pr.get
    else
      throw new Exception("Parser failure")
  }

  @Before def setUp() {
    val vll = VllParsers.fromFile(new File("./ArithExpr.vll"))
    parser = vll.phrase(vll.getParserFor("Expr"))
  }

  @Test def addition() {
    val parseResult = parse("33 + 55")
    val expectedValue = Array(Array(Pair(0, "33"), Nil), List(Pair(0, Array(Pair(0, "55"), Nil))))
    assertTrue(areSameAST(parseResult, expectedValue))
  }
  @Test def subtraction() {
    val parseResult = parse("63 - 21")
    val expectedValue = Array(Array(Pair(0, "63"), Nil), List(Pair(1, Array(Pair(0, "21"), Nil))))
    assertTrue(areSameAST(parseResult, expectedValue))
  }
  @Test def multiplication() {
    val parseResult = parse("11 * 12")
    val expectedValue = Array(Array(Pair(0, "11"), List(Pair(0, Pair(0, "12")))), Nil)
    assertTrue(areSameAST(parseResult, expectedValue))
  }
  @Test def division() {
    val parseResult = parse("132 / 11")
    val expectedValue = Array(Array(Pair(0, "132"), List(Pair(1, Pair(0, "11")))), Nil)
    assertTrue(areSameAST(parseResult, expectedValue))
  }
}

object ApiJUnitTest01 {
  def main(args: Array[String]) {
    val res = JUnitCore.runClasses(classOf[ApiJUnitTest01])
    println(res.getFailureCount)
    res.getFailures.foreach(println)
  }
}
