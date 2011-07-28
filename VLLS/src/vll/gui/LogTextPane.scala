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

import java.awt.Color
import java.awt.Font
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Component
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.ScrollPane
import scala.swing.SplitPane
import scala.swing.TextArea

class LogTextPane(val gui: VllGui) extends SplitPane(Orientation.Vertical) {
  def logOutput(msg: String) {
    logArea.setSelectionStart(logArea.getDocument.getLength)
    logArea.setSelectionEnd(logArea.getDocument.getLength)
    logArea.getDocument.insertString(logArea.getDocument.getLength, msg, blackFont)
    logArea.setSelectionStart(logArea.getDocument.getLength)
    logArea.setSelectionEnd(logArea.getDocument.getLength)
  }
  def logError(msg: String) {
    logArea.setSelectionStart(logArea.getDocument.getLength)
    logArea.setSelectionEnd(logArea.getDocument.getLength)
    logArea.getDocument.insertString(logArea.getDocument.getLength, msg, redFont)
    logArea.setSelectionStart(logArea.getDocument.getLength)
    logArea.setSelectionEnd(logArea.getDocument.getLength)
  }
  val clearLogAction = new Action("") {
    icon = swing.Swing.Icon(getClass.getResource("images/Delete16.gif"))
    toolTip = "Clear log area"
    def apply() = {logArea.setText("")}
  }
  val copyLogAction = new Action("") {
    icon = swing.Swing.Icon(getClass.getResource("images/Copy16.gif"))
    toolTip = "Copy log text"
    def apply() = {logArea.selectAll(); logArea.copy()}
  }
  def clearLogText() {logArea.setText("")}
  def copyLog() {logArea.selectAll(); logArea.copy();}
  def getText() = inputArea.text
  def cut() {inputArea.cut()}
  def copy() {inputArea.copy()}
  def paste() {inputArea.peer.paste()}
  def clear() {inputArea.text = ""}
  def selectAll() {inputArea.selectAll()}
  val inputArea = new TextArea() {
    font = new Font(Font.MONOSPACED, font.getStyle, font.getSize)
    peer.addMouseListener(new InputPopupListener(new InputPopupMenu(peer)))
    def getText = text
    def setText(t: String) {text = t}
  }
  val logArea = new JTextPane {
    setFont(new Font(Font.MONOSPACED, getFont.getStyle, getFont.getSize))
    setEditable(false)
    //def getText already defined in parent
    //def setText(t: String) already defined in parent
  }
  leftComponent = new BorderPanel {
    val pnl = new BorderPanel() {
      val runButton = new Button(gui.runParseInputAction) {text = ""}
      val stopButton = new Button(gui.stopAction) 
      add(runButton, BorderPanel.Position.West)
      add(stopButton, BorderPanel.Position.East)
    }
    add(new Label("Parser Test Input"), BorderPanel.Position.North)
//    add(pnl, BorderPanel.Position.South)
    add(new ScrollPane(inputArea), BorderPanel.Position.Center)
  }
  rightComponent = new BorderPanel {
    val pnl = new BorderPanel() {
      val clearButton = new Button(clearLogAction) 
      val copyButton = new Button(copyLogAction) 
      add(clearButton, BorderPanel.Position.West)
      add(copyButton, BorderPanel.Position.East)
    }
    add(new Label("Parser Log"), BorderPanel.Position.North)
//    add(pnl, BorderPanel.Position.South)
    add(new ScrollPane(Component.wrap(logArea)), BorderPanel.Position.Center)
  }
  dividerLocation = gui.frameWidth / 3
  private val blackFont = new SimpleAttributeSet() {
    StyleConstants.setFontFamily(this, "monospaced")
    StyleConstants.setFontSize(this, 12)
    StyleConstants.setForeground(this, Color.black)
  }
  private val redFont = new SimpleAttributeSet() {
    StyleConstants.setFontFamily(this, "monospaced")
    StyleConstants.setFontSize(this, 12)
    StyleConstants.setForeground(this, Color.red)
  }
}
