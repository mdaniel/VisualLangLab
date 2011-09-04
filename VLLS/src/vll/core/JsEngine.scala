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

package vll.core

import scala.collection._
import javax.script.Compilable
import javax.script.Invocable
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import scala.swing.TextComponent
import sun.org.mozilla.javascript.internal.NativeArray
import sun.org.mozilla.javascript.internal.NativeObject
import vll.gui.VllGui

object JsEngine {
  private val sem = new ScriptEngineManager
  private val engine = sem.getEngineByName("javascript")
  private val invocable = engine.asInstanceOf[Invocable]
  private val compilable = engine.asInstanceOf[Compilable]
  private val context = engine.getContext
  context.setAttribute("VLL", new NativeObject(), ScriptContext.ENGINE_SCOPE)
//  context.setAttribute("InputArea", VllGui.top.logTextPane.inputArea, ScriptContext.ENGINE_SCOPE)
//  context.setAttribute("LogArea", VllGui.top.logTextPane.logArea, ScriptContext.ENGINE_SCOPE)
//  context.setAttribute("VLLARGS", null, ScriptContext.ENGINE_SCOPE)
//  context.setAttribute("VLLINPUT", null, ScriptContext.ENGINE_SCOPE)
//  def eval(s: String) = engine.eval(s)
//  def invoke(fName: String, args: Any*) = invocable.invokeFunction(fName, args.map(_.asInstanceOf[Object]):_*)
  private def objToJsArray(tree: Any): Object = {
    def nativeArray(na: Array[Object]) = new NativeArray(na) {
      override def toString = getDefaultValue(null)
      override def getDefaultValue(claz: Class[_]) = {
        val b = mutable.Buffer[Object]()
        for (i <- 0 until getLength.asInstanceOf[Int]) {
          b.append(get(i, this) match {
              case narr: NativeArray => "[" + narr.getDefaultValue(null) + "]"
              case x => x
            })
        }
        b.mkString(",")
      }
    }
    val rv = tree match {
      case null => null
      case a: Array[_] => nativeArray(a.map(objToJsArray).toArray[Object])
      case p: Pair[_, _] => objToJsArray(Array[Any](p._1, p._2))
      case opt: Option[_] => objToJsArray(if (opt.isEmpty) Array[Any]() else Array[Any](opt.get))
      case lst: List[_] => objToJsArray(lst.map(objToJsArray).toArray)
      case s: String => s
      case i: Int => java.lang.Double.valueOf(i) // produced only by "combine-choice" parser 
      case r: AnyRef => r
    }
    rv
  }

  def compile(sName: String): VllParsers.ActionType = {
    val cs = compilable.compile("(%s)(VLLARGS)".format(sName))
    new Function5[TextComponent,TextComponent,Int,Int,Any,Any] {
      def apply(input: TextComponent, log: TextComponent, line: Int, col: Int, arg: Any): Any = {
        cs.getEngine.put("InputArea", input)
        cs.getEngine.put("LogArea", log)
        cs.getEngine.put("VLLARGS", objToJsArray(arg))
        cs.getEngine.put("$line", line)
        cs.getEngine.put("$col", col)
        cs.eval()
      }
    }
  }
//  def put(key: String, value: Any) = engine.put(key, value.asInstanceOf[Object])
}
