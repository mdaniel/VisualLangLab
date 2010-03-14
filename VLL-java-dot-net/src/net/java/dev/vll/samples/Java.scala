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

package net.java.dev.vll.samples

import net.java.dev.vll.Parsers._
import net.java.dev.vll.FileIO
import net.java.dev.vll.TextPane
import net.java.dev.vll.VisualLangLab
import scala.swing.Dialog

object Java {

  def load() {
    val msg = """Java1.5 Parser Under Construction"""

    FileIO.load(grammar)
    TextPane.clearLogText()
    println(msg)
    Dialog.showMessage(VisualLangLab.splitPane, 
        "Java1.5 (Incomplete)",
        "VisualLangLab sample - Java1.5", Dialog.Message.Info, null)
  }

  val grammar = <VLL-Grammar>
  <Whitespace>\s+</Whitespace>
  <Comments></Comments>
  <Tokens>
  </Tokens>
  <Parsers>
    <Parser Name="NoName">
    </Parser>
  </Parsers>
</VLL-Grammar>

}

