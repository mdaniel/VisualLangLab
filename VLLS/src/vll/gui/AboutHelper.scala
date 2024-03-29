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

package vll.gui

import scala.swing.BorderPanel
import scala.swing.Dialog
import scala.swing.Label

class AboutHelper(gui: VllGui) {
    private val vllCopyright = """<html><body><pre>
      |<b>VisualLangLab</b>
      |A Visual Parser-Generator using Scala Combinators
      |(<i>http://vll.java.net/</i> and <i>https://vll.dev.java.net/</i>)

      |Copyright 2004, 2010, Sanjay Dasgupta (sanjay.dasgupta@gmail.com)

      |VisualLangLab is free software: you can redistribute it and/or
      |modify it under the terms of the GNU General Public License as
      |published by the Free Software Foundation, either version 3 of
      |the License, or (at your option) any later version.

      |VisualLangLab is distributed in the hope that it will be useful,
      |but WITHOUT ANY WARRANTY; without even the implied warranty of
      |MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
      |GNU General Public License for more details.

      |You should have received a copy of the GNU General Public License
      |along with VisualLangLab. If not, see http://www.gnu.org/licenses/

      |<u>Other copyright notices:</u>
      |VisualLangLab depends on certain other software listed below:
      |  1) The Scala Programming Language from http://www.scala-lang.org/
      |  2) Java look and feel Graphics Repository from
      |       http://java.sun.com/developer/techDocs/hi/repository/
      |Copyright notices for these products are included in
      |the VisualLangLab distribution zip file, and should be
      |reviewed by the user.
      |</pre></body></html>"""

    def presentAboutDialog() {
      Dialog.showMessage(gui.contents(0), aboutPanel.peer, "VisualLangLab", Dialog.Message.Info, null)
    }
    private val aboutPanel: BorderPanel = new BorderPanel() {
      private val label = new Label(vllCopyright.stripMargin)
      add(label, BorderPanel.Position.Center)
    }
}
