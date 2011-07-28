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

object Utils {

  def unEscape(es: CharSequence) = {
    var buf = ""
    val sb = new StringBuilder()
    var i = 0
    while (i < es.length) {
      val c = es.charAt(i)
      (buf.length, c) match {
        case (0, '\\') => buf = "\\"
        case (0, _) => sb.append(c)
        // Standard Java escaped codes ...
        case (1, 'n') => buf = ""; sb.append('\n')
        case (1, 'r') => buf = ""; sb.append('\r')
        case (1, 't') => buf = ""; sb.append('\t')
        case (1, 'f') => buf = ""; sb.append('\f')
        case (1, 'b') => buf = ""; sb.append('\b')
        case (1, '\'') => buf = ""; sb.append('\'')
        case (1, '\"') => buf = ""; sb.append('\"')
        case (1, '\\') => buf = ""; sb.append('\\')
        case (1, n) if n >= '0' && n <= '7' => buf += n
//        case (1, _) => buf = ""; sb.append("\\" + c)
        case (2, n) if n >= '0' && n <= '7' => buf += n
        case (2, _) => sb.append(Integer.parseInt(buf.substring(1), 8).asInstanceOf[Char]); buf = ""
          if (c == '\\') buf = "\\" else sb.append(c)
        case (3, n) if n >= '0' && n <= '7' => buf += n
        case (3, _) => sb.append(Integer.parseInt(buf.substring(1), 8).asInstanceOf[Char]); buf = ""
          if (c == '\\') buf = "\\" else sb.append(c)
        case (4, _) => sb.append(Integer.parseInt(buf.substring(1), 8).asInstanceOf[Char]); buf = ""
          if (c == '\\') buf = "\\" else sb.append(c)
        case _ => throw new IllegalArgumentException("Bad escape: '%s%c'".format(buf, c))
      }
      i += 1
    }
    sb.toString
  }

  def reEscape(s: CharSequence) = {
    val sb = new StringBuilder()
    var i = 0
    while (i < s.length) {
      val c = s.charAt(i)
        c match {
        case '\n' => sb.append("\\n")
        case '\r' => sb.append("\\r")
        case '\t' => sb.append("\\t")
        case '\f' => sb.append("\\f")
        case '\b' => sb.append("\\b")
        case '\'' => sb.append("\\\'")
        case '\"' => sb.append("\\\"")
        case '\\' => sb.append("\\")
        case x if x < ' ' || x == '\377' => sb.append("\\%03o".format(x.asInstanceOf[Int] & 0x0ff))
        case _ => sb.append(c)
      }
      i += 1
    }
    sb.toString
  }
  
  def isJavascriptCode(s: String) = isJavascriptPredicate(s) | isJavascriptAction(s)
  def isScalaCode(s: String) =  isScalaAction(s) | isScalaPredicate(s)

  def isPredicateCode(s: String) = isJavascriptPredicate(s) | isScalaPredicate(s)
  def isActionCode(s: String) =  isJavascriptAction(s) | isScalaAction(s)

  def isScalaAction(s: String) =  s.trim.matches("(?s)\\(\\s*[a-zA-Z0-9_]+\\s*\\:\\s*[a-zA-Z0-9_]+\\s*\\)\\s*\\=\\>.+") 
  def isScalaPredicate(s: String) =  s.trim.matches("(?s)\\(\\s*\\)\\s*\\=\\>.+") 

  def isJavascriptAction(s: String) =  s.trim.matches("(?s)function\\s*\\(\\s*[a-zA-Z0-9_]+\\s*\\)\\s*\\{.+\\}") 
  def isJavascriptPredicate(s: String) =  s.trim.matches("(?s)function\\s*\\(\\s*\\)\\s*\\{.+\\}") 

}
