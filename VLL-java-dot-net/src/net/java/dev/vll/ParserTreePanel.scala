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

import javax.swing.{JTree, ToolTipManager}
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.{TreePath, DefaultTreeModel, DefaultMutableTreeNode, TreeSelectionModel}
import scala.swing.Dialog
import swing.{ScrollPane, Component}
import java.awt.event.{ActionListener, ActionEvent}

object ParserTreePanel extends ScrollPane with ActionListener with TreeSelectionListener {

  def valueChanged(te: TreeSelectionEvent) {
    val node = te.getPath.getLastPathComponent.asInstanceOf[ParserTreeNode]
//    val t = TypeDeducer.getType(node)
//    println(t)
  }

  def setErrorMessage() {
    Dialog.showInput(VisualLangLab.splitPane, "Enter message", "Error message", Dialog.Message.Question,
                     null, Array[String](), selectedNode.errorMessage) match {
      case Some(msg) => selectedNode.errorMessage = msg
      case None => 
    }
  }

  def chooseToken(): Option[ParserTreeNode] = {
    if (TokenBank.isEmpty) {
      Dialog.showMessage(VisualLangLab.splitPane, "No tokens defined yet", "Add token", Dialog.Message.Info, null)
      None
    } else {
      val tokenNames = TokenBank.getTokenNames
      val selection = Dialog.showInput(VisualLangLab.splitPane, "Select token", "Add token",
          Dialog.Message.Question, null, tokenNames, tokenNames(0))
      selection map (s =>
        TokenBank(s) match {
          case Right(_) => new RegexNode(s)
          case Left(_) => new LiteralNode(s)
        }
      )
    }
  }

  def chooseParser(): Option[ReferenceNode] = {
    if (ParserBank.isEmpty)
      None
    else {
      val parserNames = ParserBank.getParserNames
      val selection = Dialog.showInput(VisualLangLab.splitPane, "Select parser", "Add parser",
          Dialog.Message.Question, null, parserNames, parserNames(0))
      selection map (s => new ReferenceNode(s))
    } 
  }

  def actionPerformed(e: ActionEvent) {
    val parent = selectedNode.getParent.asInstanceOf[DefaultMutableTreeNode]
    e.getSource match {
      case TreeNodePopupMenu.newToken => chooseToken() match {
          case Some(newNode) => theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
            theModel.nodeChanged(selectedNode)
            theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
            theTree.scrollPathToVisible(new TreePath(newNode.getPath()))
          VisualLangLab.isDirty = true
          case None =>
        }
      case TreeNodePopupMenu.newSequence => val newNode = new SequenceNode();
        theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
        theModel.nodeChanged(selectedNode)
        theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
        theTree.scrollPathToVisible(new TreePath(newNode))
        VisualLangLab.isDirty = true
      case TreeNodePopupMenu.newRepSep => val newNode = new RepSepNode();
        theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
        theModel.nodeChanged(selectedNode)
        theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
        theTree.scrollPathToVisible(new TreePath(newNode))
        VisualLangLab.isDirty = true
      case TreeNodePopupMenu.newChoice => val newNode = new ChoiceNode();
        theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
        theModel.nodeChanged(selectedNode)
        theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
        theTree.scrollPathToVisible(new TreePath(newNode))
        VisualLangLab.isDirty = true
      case TreeNodePopupMenu.newReference => chooseParser() match {
          case Some(newNode) => theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
            theModel.nodeChanged(selectedNode)
            theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
            theTree.scrollPathToVisible( new TreePath(newNode))
            VisualLangLab.isDirty = true
          case None =>
        }
      case TreeNodePopupMenu.multOne => selectedNode.multiplicity = Multiplicity.One; theModel.nodeChanged(selectedNode)
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.multZeroOrOne => selectedNode.multiplicity = Multiplicity.ZeroOrOne; theModel.nodeChanged(selectedNode)
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.multZeroOrMore => selectedNode.multiplicity = Multiplicity.ZeroOrMore; theModel.nodeChanged(selectedNode)
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.multOneOrMore => selectedNode.multiplicity = Multiplicity.OneOrMore; theModel.nodeChanged(selectedNode)
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.multZero => selectedNode.multiplicity = Multiplicity.Zero; theModel.nodeChanged(selectedNode)
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.moveUp => parent.insert(selectedNode, parent.getIndex(selectedNode) - 1)
          theModel.nodeStructureChanged(parent)
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.moveDown => parent.insert(selectedNode, parent.getIndex(selectedNode) + 1)
          theModel.nodeStructureChanged(parent)  
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.errorMsg => setErrorMessage()
      case TreeNodePopupMenu.traceNode => selectedNode.trace = !selectedNode.trace
        theModel.nodeChanged(selectedNode)
      case TreeNodePopupMenu.cutNode =>
          clipboard = selectedNode
          theModel.removeNodeFromParent(selectedNode)
          theModel.nodeStructureChanged(parent)
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.copyNode =>
        clipboard = selectedNode.cloneTree
        selectedNode match {
          case s: SequenceNode => TreeNodePopupMenu.pasteNode.setEnabled(true)
          case c: ChoiceNode => TreeNodePopupMenu.pasteNode.setEnabled(true)
          case _ =>
        }
        TreeNodePopupMenu.pasteNode.setEnabled(true)
      case TreeNodePopupMenu.pasteNode => val newNode = clipboard.cloneTree
          theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
          theModel.nodeChanged(selectedNode)
          theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
          theTree.scrollPathToVisible(new TreePath(newNode.getPath()))
          VisualLangLab.isDirty = true
      case TreeNodePopupMenu.deleteNode =>
          theModel.removeNodeFromParent(selectedNode)
          theModel.nodeStructureChanged(parent)
          VisualLangLab.isDirty = true
        case TreeNodePopupMenu.type1Node => println(TypeDeducer.getType(selectedNode, 1))
        //case TreeNodePopupMenu.type2Node => println(TypeDeducer.getType(selectedNode, 2))
        case TreeNodePopupMenu.type3Node => println(TypeDeducer.getType(selectedNode, 3))
        case TreeNodePopupMenu.typeNNode => println(TypeDeducer.getType(selectedNode, Int.MaxValue))
    }
  }

  def setParser(newRoot: ParserTreeNode) {
    //theTree.removeTreeSelectionListener(this)
    theTree.removeMouseListener(popupListener)
    theTree.clearSelection()
    rootNode = newRoot
    selectedNode = rootNode
    theModel.setRoot(newRoot)
    theTree.treeDidChange()
    //theTree.addTreeSelectionListener(this)
    theTree.addMouseListener(popupListener)
    theTree.addSelectionPath(new TreePath(rootNode))
    for (i <- 1 until 20)
      theTree.expandRow(i)
  }

  var clipboard: ParserTreeNode = _
  var rootNode: ParserTreeNode = new RootNode("NoName")
  var selectedNode: ParserTreeNode = rootNode
  var parent = selectedNode.getParent
  val theModel = new DefaultTreeModel(rootNode)
  val theTree = new JTree(theModel)
  theTree.getSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
  theTree.addTreeSelectionListener(this)
  val popupListener = new TreeNodePopupListener(TreeNodePopupMenu)
  theTree.addMouseListener(popupListener)
  theTree.setShowsRootHandles(true)
  theTree.setCellRenderer(ElementRenderer)
  ToolTipManager.sharedInstance().registerComponent(theTree)
  contents = Component.wrap(theTree)
}
