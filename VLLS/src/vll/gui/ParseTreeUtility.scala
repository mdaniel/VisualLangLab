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

import vll.core.Multiplicity
import vll.core.RegexNode
import vll.core.ParserTreeNode
import vll.core.JsEngine
import vll.core.LiteralNode
import vll.core.RootNode
import vll.core.ScalaEngine
import vll.core.SequenceNode
import vll.core.RepSepNode
import vll.core.ReferenceNode
import vll.core.PredicateNode
import java.awt.Font
import javax.script.ScriptException
import scala.collection.mutable.Stack
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.ButtonGroup
import scala.swing.Dialog
import scala.swing.GridPanel
import scala.swing.Label
import scala.swing.Orientation
import scala.swing.RadioButton
import scala.swing.ScrollPane
import scala.swing.TextArea
import scala.swing.event.ButtonClicked
import scala.swing.SplitPane
import scala.swing.Swing._
import vll.core.ChoiceNode
import vll.core.Utils

class ParseTreeUtility(private val gui: VllGui) extends BorderPanel {
  private val astStructurePanel = new BorderPanel {
    val astStructTextArea = new TextArea()
    astStructTextArea.editable = false
    astStructTextArea.font = new Font(Font.MONOSPACED, astStructTextArea.font.getStyle, astStructTextArea.font.getSize)
    var displayDepth = Int.MaxValue
    private val rb1 = new RadioButton("1") {
      reactions += {
        case ButtonClicked(_) => displayDepth = 1
          displayAstStruct(/* false */)
      }
    }
    private val rb3 = new RadioButton("3") {
      reactions += {
        case ButtonClicked(_) => displayDepth = 3
          displayAstStruct(/* false */)
      }
    }
    private val rb5 = new RadioButton("5") {
      reactions += {
        case ButtonClicked(_) => displayDepth = 5
          displayAstStruct(/* false */)
      }
    }
    private val rbFull = new RadioButton("Full") {
      selected = true
      tooltip = "Selected clause only"
      reactions += {
        case ButtonClicked(_) => displayDepth = Int.MaxValue
          displayAstStruct(/* false */)
      }
    }
    private val bg = new ButtonGroup(rb1, rb3, rb5, rbFull)
    private val southPanel2 = new GridPanel(1, 6) {
      contents.append(rb1, rb3, rb5, rbFull)
    }
    private val southPanel = new BorderPanel {
      add(new Label(" Depth:"), BorderPanel.Position.West)
      add(southPanel2, BorderPanel.Position.Center)
    }
    private val titleLabel = new Label("Parse Tree (AST) Structure")
    add(titleLabel, BorderPanel.Position.North)
    add(new ScrollPane(astStructTextArea), BorderPanel.Position.Center)
    add(southPanel, BorderPanel.Position.South)
    def isGlobal = false //rbn.selected
  }
  private val actionTextPanel = new BorderPanel {
    def setNode(n: ParserTreeNode) {
      actionTextArea.enabled = !n.isInstanceOf[RootNode]
      btnPanel.saveBtn.enabled = actionTextArea.enabled
      btnPanel.codeBtn.enabled = actionTextArea.enabled
      if ((node != null) && node.actionText != actionTextArea.text) {
        //Dialog.showMessage(VllGui.top.contents(0), "Inside if", "Inside if", Dialog.Message.Plain, null)
        if (Dialog.showConfirmation(VllGui.top.contents(0), "Save changes?", "Unsaved changes exist", 
          Dialog.Options.OkCancel, Dialog.Message.Question, null) == Dialog.Result.Ok) {
          if (actionTextArea.text.trim.isEmpty)
            node.actionText = ""
          else
            node.actionText = actionTextArea.text
          VllGui.top.parserTreePanel.nodeChanged()
        }
      }
      node = n
      actionTextArea.text = node.actionText
    }
    private var node: ParserTreeNode = null
    private val actionTextArea = new TextArea
    private val btnPanel = new BorderPanel() {
      val saveBtn = new Button("Save") {
        reactions += {
          case ButtonClicked(_) => 
            val actionText = actionTextArea.text
            val fmtOk = if (node.isInstanceOf[PredicateNode]) Utils.isPredicateCode(actionText) else Utils.isActionCode(actionText)
            if (fmtOk) {
              val isJS = actionText.trim.startsWith("function")
              try {
                if (isJS) JsEngine.compile(actionText) else ScalaEngine.compile(actionText)
                node.actionText = actionText
                VllGui.top.parserTreePanel.nodeChanged()
                Dialog.showMessage(VllGui.top.contents(0), "Syntax OK - text saved", "OK, saved", Dialog.Message.Info, null)
                gui.isDirty = true
              } catch {
                case se: Exception => 
                val msg = se.getMessage
                Dialog.showMessage(VllGui.top.contents(0), msg.substring(msg.indexOf(": ") + 2), 
                    "Script syntax error", Dialog.Message.Error, null)
              }
            } else if (actionText.trim.isEmpty) {
              node.actionText = ""
              VllGui.top.parserTreePanel.nodeChanged()
            } else
              Dialog.showMessage(VllGui.top.contents(0), "Click \"Code\" button for outline code", "Action format error", Dialog.Message.Error, null)
        }
      }
      val codeBtn = new Button("Code") {
        reactions += {
          case ButtonClicked(_) => 
            val theCode = if (node.isInstanceOf[PredicateNode]) predicateCode else actionCode
            if (actionTextArea.text.trim.isEmpty)
              actionTextArea.text = theCode
            else 
              actionTextArea.text = theCode + "\n\n" + actionTextArea.text
        }
      }
      add(codeBtn, BorderPanel.Position.West)
      add(saveBtn, BorderPanel.Position.East)
      val actionCode = "function (ast) {\n" + "  if (ast) {\n" +
              "    //\n    // var ast2 = ... (AST processing code)\n    //\n" + 
              "    return ast2; // processed AST\n" + "  } else {\n" +
              "    //\n    // pre-parse (initialization) code\n    //\n" + "  }\n}\n"
      val predicateCode = "function () {\n" + 
              "  //\n  // var ok = ... (predicate computation code)\n  //\n" + 
              "  if (ok) // check the predicate (ok) value\n    return true; // Parsing success\n  else\n" +
              "    return \"(error message)\"; // Parsing failure\n}\n"
    }
    private val titleLabel = new Label("Action Text")
    add(titleLabel, BorderPanel.Position.North)
    add(new ScrollPane(actionTextArea), BorderPanel.Position.Center)
    add(btnPanel, BorderPanel.Position.South)
  }
//  private var displayedNode: ParserTreeNode = null
  private var displayedNode = gui.parserTreePanel.selectedNode.pNode
  private val spacer = "\u00a6  "
  add(new SplitPane(Orientation.Vertical, astStructurePanel, actionTextPanel), 
      BorderPanel.Position.Center)

  def displayAstStruct(/* reset: Boolean = true */) {
    displayedNode = gui.parserTreePanel.selectedNode.pNode
    actionTextPanel.setNode(displayedNode)
    if (displayedNode ne null) {
      astStructurePanel.astStructTextArea.text = getType(displayedNode, astStructurePanel.displayDepth)
    }
  }
  
  private def indent(s: String) = {
    val sb = new StringBuilder
    sb.append(spacer)
    s.foreach(c => {sb.append(c); if (c == '\n') sb.append(spacer)})
    sb.toString
  }

  private val stack = new Stack[ParserTreeNode]

  private def removeMargin(s: String) = {
    if (s.startsWith(spacer))
      s.substring(s.lastIndexOf(spacer) + spacer.length)
    else
      s
  }

  private def applyMultiplicity(t: String, node: ParserTreeNode, margin: String): String = node.multiplicity match {
    case Multiplicity.One | Multiplicity.Guard | Multiplicity.Not => t
    case Multiplicity.OneOrMore =>
      if (t.contains("\n"))
        "%sList(\n%s\n%s)".format(margin, indent(t), margin)
      else
        "%sList(%s)".format(margin, removeMargin(t))
    case Multiplicity.ZeroOrMore =>
      if (t.contains("\n"))
        "%sList(\n%s\n%s)".format(margin, indent(t), margin)
      else
        "%sList(%s)".format(margin, removeMargin(t))
    case Multiplicity.ZeroOrOne => 
      if (t.contains("\n"))
        "%sOption(\n%s\n%s)".format(margin, indent(t), margin)
      else
        "%sOption(%s)".format(margin, removeMargin(t))
  }

  private def getType(node: ParserTreeNode, depthLimit: Int) = {
    //stack = new Stack()
    stack.clear()
    astType(node, 0, depthLimit)
  }

  private def retType(node: ParserTreeNode, depth: Int, limit: Int): String = {
    if (!node.actionText.isEmpty) {
      val margin = spacer * depth
      margin + "js@%s".format(node.nodeName)
    } else
      astType(node, depth, limit)
  }
  
  private def astType(node: ParserTreeNode, depth: Int, limit: Int): String = {
    val margin = spacer * depth
    if (depth > limit)
      margin + "_"
    else if (!node.isValid)
      margin + "?"
    else {
      if (astStructurePanel.isGlobal)
        stack.push(node)
      val basicType: String = node match {
        case LiteralNode(_, name) => margin + "\"%s\"".format(gui.parsers.tokenBank(name).left.get)
        case RegexNode(_, name) => margin + "[%s]".format(name)
        case root: RootNode => retType(root.head, depth, limit) 
        case seq: SequenceNode => 
          def dropped(n: ParserTreeNode) = n.drop || n.multiplicity == Multiplicity.Not || n.multiplicity == Multiplicity.Guard || n.isInstanceOf[PredicateNode]
          val effectiveLength = seq.filter(!dropped(_)).size
          if (effectiveLength == 1) {
            retType(seq.filter(!dropped(_)).head, depth, limit)
          } else {
            val ts = seq.filter(!dropped(_)).map(retType(_, depth + 1, limit))
            ts.mkString((margin + "Array(\n"), ",\n", ("\n" + margin + ")"))
          }
        case choice: ChoiceNode =>
          def mkPair(p: Pair[String, Int]) = {
            if (p._1.contains("\n")) {
              "%s%sPair(%d,\n%s\n%s%s)".format(margin, spacer, p._2, indent(p._1), spacer, margin)
              //format("%s%sPair(%d,\n%s%s\n%s%s)", margin, spacer, p._2, margin, indent(p._1), spacer, margin)
            } else
              "%s%sPair(%d, %s)".format(margin, spacer, p._2, removeMargin(p._1))
          }
          val rt = choice.map(n => retType(n, depth + 1, limit))
          rt.zipWithIndex.map(t => mkPair(t)).
          mkString((margin + "Choice(\n"), ",\n", ("\n" + margin + ")"))
        case rs: RepSepNode => retType(rs.head, depth, limit)
        case ReferenceNode(_, parserName) => 
          if (astStructurePanel.isGlobal) {
            val that = gui.parsers.parserBank(parserName)
            if (stack.contains(that)) (margin + "@" + parserName) else retType(that, depth, limit)
          } else
            margin + "@" + parserName
        case rs: PredicateNode => "" 
      }
      if (astStructurePanel.isGlobal)
        stack.pop()
      applyMultiplicity(basicType, node, margin)
    }
  }
  displayAstStruct()
}
