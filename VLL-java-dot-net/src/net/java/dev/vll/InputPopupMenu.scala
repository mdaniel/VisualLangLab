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

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JMenuItem
import javax.swing.JPopupMenu

object InputPopupMenu extends JPopupMenu with ActionListener {

  val cutMenuItem = new JMenuItem("Cut")
  add(cutMenuItem)
  cutMenuItem.addActionListener(this)

  val copyMenuItem = new JMenuItem("Copy")
  add(copyMenuItem)
  copyMenuItem.addActionListener(this)

  val pasteMenuItem = new JMenuItem("Paste")
  add(pasteMenuItem)
  pasteMenuItem.addActionListener(this)

  val deleteMenuItem = new JMenuItem("Delete")
  add(deleteMenuItem)
  deleteMenuItem.addActionListener(this)

  def actionPerformed(ae: ActionEvent) {
    ae.getSource match {
      case `cutMenuItem` => TextPane.cut()
      case `copyMenuItem` => TextPane.copy()
      case `pasteMenuItem` => TextPane.paste()
      case `deleteMenuItem` => TextPane.delete()
    }
  }

}
