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

import javax.swing.{JTree, ToolTipManager, DropMode}
import javax.swing.event.TreeSelectionEvent
import javax.swing.event.TreeSelectionListener
import javax.swing.tree.{TreePath, DefaultTreeModel, TreeSelectionModel}
import scala.collection._
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.Dialog
import scala.swing.GridPanel
import scala.swing.Swing._
import swing.{ScrollPane, Component}
import java.awt.event.{ActionListener, ActionEvent}
import vll.core.ChoiceNode
import vll.core.LiteralNode
import vll.core.Multiplicity
import vll.core.ReferenceNode
import vll.core.RegexNode
import vll.core.RepSepNode
import vll.core.SequenceNode
import vll.core.PredicateNode
import vll.core.VllParsers

class RuleTreePanel(val guiTop: VllGui, val hub: VllParsers) extends BorderPanel with ActionListener with TreeSelectionListener {

  implicit def VllGui2Component(gui: VllGui): Component = Component.wrap(gui.peer.getRootPane)

  def valueChanged(te: TreeSelectionEvent) {
    try {
      selectedNode = te.getPath.getLastPathComponent.asInstanceOf[GuiNode]
      guiTop.typeDisplayPane.displayAstStruct(/* selectedNode.pNode */)
    } catch {
      case x => x.printStackTrace(); System.out.flush()
    }
  }

  def setErrorMessage() {
    Dialog.showInput(guiTop, "Enter message", "Error message", Dialog.Message.Question,
                     null, Array[String](), selectedNode.errorMessage) match {
      case Some(msg) => selectedNode.errorMessage = msg.asInstanceOf[String]
      case None => 
    }
  }
  
  def setDescription() {
    Dialog.showInput(guiTop, "Enter Description", "Node Description", Dialog.Message.Question,
                     null, Array[String](), selectedNode.description) match {
      case Some(descr) => selectedNode.description = descr.asInstanceOf[String]
      case None => 
    }
  }
  
  def chooseToken(): Option[GuiNode] = {
    if (hub.tokenBank.isEmpty) {
      Dialog.showMessage(guiTop, "No tokens defined yet", "Add token", Dialog.Message.Info, null)
      None
    } else {
      val tokenNames = hub.tokenBank.getTokenNames
      val selection = Dialog.showInput(guiTop.splitPane, "Select token", "Add token",
          Dialog.Message.Question, null, tokenNames, tokenNames(0))
      selection map (s =>
        hub.tokenBank(s) match {
          case Right(_) => /* new  */GuiNode(RegexNode(Multiplicity.One, s))
          case Left(_) => /* new  */GuiNode(LiteralNode(Multiplicity.One, s))
        }
      )
    }
  }

  def chooseRule(): Option[GuiNode] = {
    if (hub.ruleBank.isEmpty)
      None
    else {
      val ruleNames = hub.ruleBank.ruleNames
      val selection = Dialog.showInput(guiTop.splitPane, "Select rule", "Add reference",
          Dialog.Message.Question, null, ruleNames, ruleNames(0))
      selection map (s => /* new  */GuiNode(ReferenceNode(Multiplicity.One, s)))
    } 
  }

  private def changeToRoot(sn: GuiNode) {
    theModel.nodeChanged(sn)
    val pn = sn.getParent
    if (pn != null)
      changeToRoot(pn)
  }
  
  def nodeChanged() = theModel.nodeChanged(selectedNode)

  def actionPerformed(e: ActionEvent) {
    val parent = selectedNode.getParent
    e.getSource match {
/*       case treeNodePopupMenu.distinguishCheck => selectedNode.distinguish = !selectedNode.distinguish
        theModel.nodeChanged(selectedNode)
 */      case treeNodePopupMenu.gotoMenuItem => selectedNode.pNode match {
          case ReferenceNode(_, ruleName) => guiTop.ruleChooser.setSelectedItem(ruleName)
          case RegexNode(_, regexName: String) =>
            val currentValue = guiTop.parsers.tokenBank(regexName).right.get
            Dialog.showInput(guiTop.splitPane, "Edit regex '%s'".format(regexName), "Edit token", Dialog.Message.Question, null, Array[String](), currentValue) match {
            case Some(newVal) =>
              if (currentValue != newVal) guiTop.validateAndAssignTokenValue(false, true, regexName, newVal.asInstanceOf[String])
            case None =>
          }
          case LiteralNode(_, literalName: String) =>
            val currentValue = guiTop.parsers.tokenBank(literalName).left.get
            Dialog.showInput(guiTop.splitPane, "Edit literal '%s'".format(literalName), "Edit token", Dialog.Message.Question, null, Array[String](), currentValue) match {
            case Some(newVal) =>
              if (currentValue != newVal) guiTop.validateAndAssignTokenValue(false, false, literalName, newVal.asInstanceOf[String])
            case None =>
          }
          case _ =>
      }
      case treeNodePopupMenu.newToken => chooseToken() match {
          case Some(newNode) => theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
            theModel.nodeChanged(selectedNode)
            theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
            theTree.scrollPathToVisible(new TreePath(newNode.getPath))
          guiTop.isDirty = true
          case None =>
        }
      case treeNodePopupMenu.newSequence => 
        val newNode = /* new  */GuiNode(SequenceNode());
        theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
        theModel.nodeChanged(selectedNode)
        theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
        theTree.scrollPathToVisible(new TreePath(newNode))
        guiTop.isDirty = true
      case treeNodePopupMenu.newRepSep => val newNode = /* new  */GuiNode(RepSepNode());
        theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
        theModel.nodeChanged(selectedNode)
        theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
        theTree.scrollPathToVisible(new TreePath(newNode))
        guiTop.isDirty = true
      case treeNodePopupMenu.newChoice => val newNode = /* new  */GuiNode(ChoiceNode());
        theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
        theModel.nodeChanged(selectedNode)
        theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
        theTree.scrollPathToVisible(new TreePath(newNode))
        guiTop.isDirty = true
      case treeNodePopupMenu.newReference => chooseRule() match {
          case Some(newNode) => theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
            theModel.nodeChanged(selectedNode)
            theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
            theTree.scrollPathToVisible(new TreePath(newNode))
            guiTop.isDirty = true
          case None =>
        }
      case treeNodePopupMenu.newPredicate => val newNode = GuiNode(PredicateNode());
        theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
        theModel.nodeChanged(selectedNode)
        theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
        theTree.scrollPathToVisible(new TreePath(newNode))
        guiTop.isDirty = true
      case treeNodePopupMenu.multOne => selectedNode.multiplicity = Multiplicity.One; //theModel.nodeChanged(selectedNode)
          guiTop.isDirty = true; changeToRoot(selectedNode)
      case treeNodePopupMenu.multZeroOrOne => selectedNode.multiplicity = Multiplicity.ZeroOrOne; //theModel.nodeChanged(selectedNode)
          guiTop.isDirty = true; changeToRoot(selectedNode)
      case treeNodePopupMenu.multZeroOrMore => selectedNode.multiplicity = Multiplicity.ZeroOrMore; //theModel.nodeChanged(selectedNode)
          guiTop.isDirty = true; changeToRoot(selectedNode)
      case treeNodePopupMenu.multOneOrMore => selectedNode.multiplicity = Multiplicity.OneOrMore; //theModel.nodeChanged(selectedNode)
          guiTop.isDirty = true; changeToRoot(selectedNode)
      case treeNodePopupMenu.multNot => selectedNode.multiplicity = Multiplicity.Not; //theModel.nodeChanged(selectedNode)
          guiTop.isDirty = true; changeToRoot(selectedNode)
      case treeNodePopupMenu.multGuard => selectedNode.multiplicity = Multiplicity.Guard; //theModel.nodeChanged(selectedNode)
          guiTop.isDirty = true; changeToRoot(selectedNode)
      case treeNodePopupMenu.errorMsg => setErrorMessage()
        theModel.nodeChanged(selectedNode)
      case treeNodePopupMenu.description => setDescription()
        theModel.nodeChanged(selectedNode)
      case treeNodePopupMenu.commitPoint =>
        val seqParent = parent.pNode.asInstanceOf[SequenceNode]
        if (seqParent.commitPoint == -1) {
          seqParent.commitPoint = parent.getIndex(selectedNode)
          theModel.nodeChanged(selectedNode)
        } else if (seqParent.commitPoint != parent.getIndex(selectedNode)) {
          val prevNode = parent.getChildAt(seqParent.commitPoint)
          seqParent.commitPoint = parent.getIndex(selectedNode)
          theModel.nodeChanged(prevNode)
          theModel.nodeChanged(selectedNode)
        } else {
          seqParent.commitPoint = -1
          theModel.nodeChanged(selectedNode)
        }
        //theModel.nodeChanged(parent)
        //parent.asInstanceOf[SequenceNode].commitPoint = parent
      case treeNodePopupMenu.dropCheck => selectedNode.drop = !selectedNode.drop
        theModel.nodeChanged(selectedNode)
        theModel.nodeChanged(parent)
      case treeNodePopupMenu.packratCheck => selectedNode.isPackrat = !selectedNode.isPackrat
        theModel.nodeChanged(selectedNode)
      case treeNodePopupMenu.traceNode => selectedNode.trace = !selectedNode.trace
        theModel.nodeChanged(selectedNode)
      case treeNodePopupMenu.cutNode =>
          clipboard = selectedNode
          theModel.removeNodeFromParent(selectedNode)
          theModel.nodeStructureChanged(parent)
          guiTop.isDirty = true
      case treeNodePopupMenu.copyNode =>
        clipboard = selectedNode.cloneTree
        selectedNode match {
          case s: SequenceNode => treeNodePopupMenu.pasteNode.setEnabled(true)
          case c: ChoiceNode => treeNodePopupMenu.pasteNode.setEnabled(true)
          case _ =>
        }
        treeNodePopupMenu.pasteNode.setEnabled(true)
      case treeNodePopupMenu.pasteNode => val newNode = clipboard.cloneTree
          theModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount)
          theModel.nodeChanged(selectedNode)
          theTree.expandPath(new TreePath(selectedNode.getPath.asInstanceOf[Array[Object]]))
          theTree.scrollPathToVisible(new TreePath(newNode.getPath))
          guiTop.isDirty = true
      case treeNodePopupMenu.deleteNode =>
          theModel.removeNodeFromParent(selectedNode)
          theModel.nodeStructureChanged(parent)
          guiTop.isDirty = true
/*         case treeNodePopupMenu.redNode =>
          selectedNode.color = Color.red
        case treeNodePopupMenu.greenNode =>
          selectedNode.color = Color.green
        case treeNodePopupMenu.blueNode =>
          selectedNode.color = Color.blue
        case treeNodePopupMenu.cyanNode =>
          selectedNode.color = Color.cyan
        case treeNodePopupMenu.magentaNode =>
          selectedNode.color = Color.magenta
        case treeNodePopupMenu.orangeNode =>
          selectedNode.color = Color.orange
        case treeNodePopupMenu.blackNode =>
          selectedNode.color = Color.black
 */    }
    guiTop.typeDisplayPane.displayAstStruct(/* selectedNode.pNode */)
  }

  def setRule(newRule: String) {
    GuiNode.clearCache()
    rootNode = GuiNode(hub.ruleBank(newRule))
    theTree.removeTreeSelectionListener(this)
    if (newRule != guiTop.lastPoppedName && (displayStack.isEmpty || newRule != displayStack(0))) {
      displayStack.push(newRule)
    }
    guiTop.backButton.enabled = displayStack.size > 1
    theTree.removeMouseListener(treeNodePopupListener)
    theTree.clearSelection()
    selectedNode = rootNode
    theModel.setRoot(rootNode)
    theTree.treeDidChange()
    theTree.addTreeSelectionListener(this)
    theTree.addMouseListener(treeNodePopupListener)
    theTree.addSelectionPath(new TreePath(rootNode))
    expandTree(rootNode)
  }

  def expandTree(node: GuiNode) {
    if (!node.isLeaf) {
      val path = node.getPath
      val treePath = new TreePath(path.asInstanceOf[Array[Object]])
      theTree.expandPath(treePath)
      node.foreach(n => expandTree(n))
    }
  }

  private val typeDeducer: RuleTreeUtility = null
  val treeNodePopupMenu = new TreeNodePopupMenu(this)
  private val treeNodePopupListener = new TreeNodePopupListener(this)
  var clipboard: GuiNode = _
  var rootNode: GuiNode = GuiNode(hub.ruleBank("Main"))
  val theModel = new DefaultTreeModel(rootNode)
  var selectedNode: GuiNode = rootNode
  private var parent: GuiNode = selectedNode.getParent
  val displayStack = new mutable.Stack[String]()
  val theTree = new JTree(theModel)
  theTree.setSelectionRow(0)
  theTree.getSelectionModel.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION)
  theTree.setDragEnabled(true)
  theTree.setDropMode(DropMode.INSERT)
  theTree.setTransferHandler(new TreeTransferHandler())
  theTree.addTreeSelectionListener(this)
  theTree.addMouseListener(treeNodePopupListener)
  theTree.setShowsRootHandles(true)
  theTree.setCellRenderer(new ElementRenderer(guiTop, hub))
  ToolTipManager.sharedInstance().registerComponent(theTree)
  val colorBtn = new Button("Trace On")
  val noColorBtn = new Button("Trace Off")
  //val noColorBtn = new Button("\u2500")
  val colorPanel = new GridPanel(1, 2) {
    contents.append(colorBtn, noColorBtn)
  }
  add(new ScrollPane(Component.wrap(theTree)), BorderPanel.Position.Center)
  //add(colorPanel, BorderPanel.Position.South)
  //contents = Component.wrap(theTree)
}
