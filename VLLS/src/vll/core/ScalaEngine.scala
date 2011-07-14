/*
 Copyright 2010, Sanjay Dasgupta
 sanjay.dasgupta@gmail.com

 This file is part of VisualLangLab (https://vll.dev.java.net/).

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

package vll.core

import java.io.OutputStream
import java.io.PrintWriter
import scala.collection._
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.IMain

object ScalaEngine {
  
  private val outputStream = new OutputStream {
    private val buf = new StringBuilder()
    override def write(b: Int) {
      buf.append(b.asInstanceOf[Char])
    }
    def clear() {buf.clear()}
    override def toString() = buf.toString
  }
  private val compiledFunctionHolder = new AnyHolder[Function3[Int,Int,Any,Any]]
  private val vll = new AnyHolder[Any]
  private val interp = new IMain(new Settings, new PrintWriter(outputStream))  
  interp.settings.usejavacp.value = true
  interp.setContextClassLoader()
//    println("setContextClassLoader() >>>" + outputStream.toString)
//    outputStream.clear()
  interp.bind("VLL", "{def value: Object; def value_=(v: Object)}", vll)
//    println("bind VLL >>>" + outputStream.toString)
//    outputStream.clear()
  interp.bind("$$", "{def value: Object; def value_=(v: Object)}", compiledFunctionHolder)
//    println("bind $$ >>>" + outputStream.toString)
//  interp.allDefinedNames.foreach(println)

  def compile(f: String): Function3[Int,Int,Any,Any] = {
//    printf("Enter compile(%s)%n", f)
    compiledFunctionHolder.value = null
    outputStream.clear()
    interp.interpret("$$.value = ($line:Int,$col:Int,%s".format(f.substring(1)))
//    println(outputStream.toString)
    if (!outputStream.toString.trim.isEmpty) {
      val msg = outputStream.toString
      if (msg.startsWith("<console>:"))
        throw new IllegalArgumentException(msg)
    }
//    printf("Exit compile(%s)%n", compiledFunctionHolder.value)
    compiledFunctionHolder.value
  }
  
}
