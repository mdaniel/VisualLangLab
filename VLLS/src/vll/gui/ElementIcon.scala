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

import java.awt.{Component, Graphics}
import javax.swing.{Icon}
import vll.core.RuleTreeNode

class ElementIcon(val node: RuleTreeNode, val selected: Boolean) extends javax.swing.Icon {

  lazy val image = (node.getClass.getSimpleName match {
    case "ChoiceNode" => Images.choice
    case "LiteralNode" => Images.literal
    case "PredicateNode" => Images.predicate
    case "ReferenceNode" => Images.reference
    case "RegexNode" => Images.regex
    case "RepSepNode" => Images.repSep
    case "RootNode" => Images.root
    case "SequenceNode" => Images.sequence
    case "WildCardNode" => Images.wildCard
  }) getImage

  lazy val getIconHeight = if (image eq null) 0 else image.getHeight(null)
  lazy val getIconWidth = if (image eq null) 0 else image.getWidth(null)
  
  def paintIcon(c: Component, g: Graphics, x: Int, y: Int ) {
    if (image ne null) g.drawImage(image, 0, 0, null)
  }
}

