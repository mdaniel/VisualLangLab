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

object JsEngine {
  private val sem = new ScriptEngineManager
  private val engine = sem.getEngineByName("javascript")
  private val invocable = engine.asInstanceOf[Invocable]
  private val compilable = engine.asInstanceOf[Compilable]
  private val context = engine.getContext
  context.setAttribute("VLL", new NativeObject(), ScriptContext.ENGINE_SCOPE)
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
    val cs = compilable.compile("(%s)(vllARGS)".format(sName))
    new VllParsers.ActionType {
      def apply(inputCharSeq: CharSequence, inputArea: TextComponent, log: TextComponent, line: Int, col: Int, offset: Int, 
                    lastMsg: String, lastLine: Int, lastCol: Int, arg: Any): Any = {
        cs.getEngine.put("vllInputCharSequence", inputCharSeq)
        cs.getEngine.put("vllInput", inputCharSeq)
        cs.getEngine.put("vllParserTestInput", inputArea)
        cs.getEngine.put("vllParserLog", log)
        cs.getEngine.put("vllARGS", objToJsArray(arg))
        cs.getEngine.put("vllLine", line)
        cs.getEngine.put("vllCol", col)
        cs.getEngine.put("vllOffset", offset)
        cs.getEngine.put("vllLastMsg", lastMsg)
        cs.getEngine.put("vllLastLine", lastLine)
        cs.getEngine.put("vllLastCol", lastCol)
        cs.eval()
      }
    }
  }
}
