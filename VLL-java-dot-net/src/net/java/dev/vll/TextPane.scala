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

package net.java.dev.vll

import java.awt.Font
import scala.swing.BorderPanel
import scala.swing.Label
import scala.swing.ScrollPane
import scala.swing.SplitPane
import scala.swing.TextArea

object InputTextHeader extends BorderPanel {
  add(new Label("Parser Input"), BorderPanel.Position.Center)
}

object LogAreaHeader extends BorderPanel {
  add(new Label("Log"), BorderPanel.Position.Center)
}

object TextPane extends SplitPane {
  def setMonospacedFont(ta: TextArea) {
    ta.font = new Font(Font.MONOSPACED, ta.font.getStyle, ta.font.getSize)
  }
  def log(t: String) {logArea.append(t)}
  def clearLogText() {logArea.text = ""}
  def copyLog() {logArea.copy()}
  def getText() = inputTextArea.text
  def cut() {inputTextArea.cut()}
  def copy() {inputTextArea.copy()}
  def paste() {inputTextArea.paste()}
  def delete() {}
  private val inputTextArea = new TextArea() 
  setMonospacedFont(inputTextArea)
  inputTextArea.peer.addMouseListener(new InputPopupListener(InputPopupMenu))
  private val logArea = new TextArea
  logArea.lineWrap = true
  logArea.peer.setWrapStyleWord(true)
  setMonospacedFont(logArea)
  logArea.editable = false
  topComponent = new BorderPanel {
    add(InputTextHeader, BorderPanel.Position.North)
    add(new ScrollPane(inputTextArea), BorderPanel.Position.Center)
  }
  bottomComponent = new BorderPanel {
    add(LogAreaHeader, BorderPanel.Position.North)
    add(new ScrollPane(logArea), BorderPanel.Position.Center)
//    add(logArea, BorderPanel.Position.Center)
  }
  dividerLocation = (VisualLangLab.frameHeight * 0.3).asInstanceOf[Int]
}
