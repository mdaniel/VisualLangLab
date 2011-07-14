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

import java.awt.{Component, Graphics}
import javax.swing.{Icon}
import vll.core.ParserTreeNode
import scala.swing.Swing

class ElementIcon(val node: ParserTreeNode, val selected: Boolean) extends javax.swing.Icon {

  def image = /* swing. */Swing.Icon(getClass.getResource("images/" +
      ElementIcon.iconImageName(node.getClass.getSimpleName))).getImage
  //def image = node.image

  lazy val getIconHeight = image.getHeight(null)
  lazy val getIconWidth = image.getWidth(null)
  
  //printf("ElementIcon: %s, Node: %s, image: %s\n", this, node, image)
  def paintIcon(c: Component, g: Graphics, x: Int, y: Int ) {
    g.drawImage(image, 0, 0, null)
    //g.drawImage(image, x, y, null)
  }
}

object ElementIcon {
  val iconImageName = Map(
    "ChoiceNode" -> "Choice.gif",
    "LiteralNode" -> "Literal.gif",
    "RegexNode" -> "Regex.gif",
    "RepSepNode" -> "RepSep.gif",
    "RootNode" -> "Root.gif",
    "ReferenceNode" -> "Reference.gif",
    "SequenceNode" -> "Sequence.gif",
    "PredicateNode" -> "SemPred.gif"
  )
}
