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

package vll.gui

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JMenuItem
import javax.swing.JPopupMenu
import javax.swing.JTextArea

class InputPopupMenu(c: JTextArea) extends JPopupMenu with ActionListener {

  val cutMenuItem = new JMenuItem("Cut")
  add(cutMenuItem)
  cutMenuItem.addActionListener(this)

  val copyMenuItem = new JMenuItem("Copy")
  add(copyMenuItem)
  copyMenuItem.addActionListener(this)

  val pasteMenuItem = new JMenuItem("Paste")
  add(pasteMenuItem)
  pasteMenuItem.addActionListener(this)

  val clearMenuItem = new JMenuItem("Clear")
  add(clearMenuItem)
  clearMenuItem.addActionListener(this)

  val selectAllMenuItem = new JMenuItem("Select all")
  add(selectAllMenuItem)
  selectAllMenuItem.addActionListener(this)

  def actionPerformed(ae: ActionEvent) {
    ae.getSource match {
      case `cutMenuItem` => c.cut()
      case `copyMenuItem` => c.copy()
      case `pasteMenuItem` => c.paste()
      case `clearMenuItem` => c.setText("")
      case `selectAllMenuItem` => c.selectAll()
    }
  }

}
