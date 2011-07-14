package vll.gui

import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler
import javax.swing.TransferHandler._
//import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
//import javax.swing.tree.TreeNode

class TreeTransferHandler extends TransferHandler {
  val mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" +
        classOf[GuiNode].getName() + "\""
  val guiNodeFlavor = new DataFlavor(mimeType);
  val flavors = Array(guiNodeFlavor);
  var nodeToRemove: GuiNode = null

  override def canImport(support: TransferHandler.TransferSupport): Boolean = {
    if(!support.isDrop)
      return false;
    support.setShowDropLocation(true)
     if(!support.isDataFlavorSupported(guiNodeFlavor))
      return false;
        // Do not allow a drop on the drag source selections.
    val dl = support.getDropLocation.asInstanceOf[JTree.DropLocation]
/*     val tree = support.getComponent.asInstanceOf[JTree]
    val dropRow = tree.getRowForPath(dl.getPath)
    printf("Drop: %d %s%n", dropRow, dl.getPath.getLastPathComponent)
    val selRows = tree.getSelectionRows
    for(i <- 0 until selRows.length) {
      if(selRows(i) == dropRow)
        return false;
    }
 */    //val nt = support.getTransferable.asInstanceOf[NodesTransferable]
    //val par2 = nodeToRemove.getParent//nt.nodes.head.getParent
    val par1 = dl.getPath.getLastPathComponent
    if ((nodeToRemove eq null) || (nodeToRemove.getParent eq null) || par1 != nodeToRemove.getParent) {
//      printf("%s = %s%n", par1, nodeToRemove.getParent)
      return false
    }
    // Do not allow MOVE-action drops if a non-leaf node is
    // selected unless all of its children are also selected.
//    val action = support.getDropAction
/*    if(action == MOVE)
      return haveCompleteNode(tree);
    // Do not allow a non-leaf node to be copied to a level
    // which is less than its source level.
     val dest = dl.getPath
    val target = dest.getLastPathComponent.asInstanceOf[GuiNode]
    val path = tree.getPathForRow(selRows(0))
    val firstNode = path.getLastPathComponent.asInstanceOf[GuiNode]
    if(firstNode.getChildCount() > 0 && target.getLevel() < firstNode.getLevel())
      return false;
 */    true;
  }

  override def createTransferable(c: JComponent): Transferable = {
    val path = c.asInstanceOf[JTree].getSelectionPath
    if (path != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            //val copies = ArrayBuffer[DefaultMutableTreeNode]()
            //val toRemove = ArrayBuffer[DefaultMutableTreeNode]()
            val node = path.getLastPathComponent.asInstanceOf[GuiNode]
            //copies += copy(node)
            //toRemove += node
         //val nodes = copies.toArray
        nodeToRemove = node
            //nodesToRemove =
            //    toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
            new NodesTransferable(node.cloneTree);

    } else
      null
  }

/*   private def copy(node: TreeNode) = new DefaultMutableTreeNode(node)

 */  override def exportDone(source: JComponent, data: Transferable, action: Int) {
    if((action & MOVE) == MOVE) {
      val model = source.asInstanceOf[JTree].getModel.asInstanceOf[DefaultTreeModel]
            // Remove nodes saved in nodesToRemove in createTransferable.
/*       for(i <- 0 until nodesToRemove.length) {
         model.removeNodeFromParent(nodesToRemove(i));
      }
 */
      model.removeNodeFromParent(nodeToRemove)
    }
  }

  override def getSourceActions(c: JComponent) = TransferHandler.MOVE

  override def importData(support: TransferHandler.TransferSupport): Boolean = {
    if(!canImport(support))
      return false
        // Extract transfer data.
    var node: GuiNode = null
    try {
       val t = support.getTransferable()
       node = t.getTransferData(guiNodeFlavor).asInstanceOf[GuiNode]
    } catch {
      case ufe: UnsupportedFlavorException => println("UnsupportedFlavor: " + ufe.getMessage());
      case ioe: java.io.IOException => println("I/O error: " + ioe.getMessage());
    }
        // Get drop location info.
    val dl = support.getDropLocation.asInstanceOf[JTree.DropLocation]
    val childIndex = dl.getChildIndex
    val dest = dl.getPath
    val parent = dest.getLastPathComponent.asInstanceOf[GuiNode]
    val tree = support.getComponent.asInstanceOf[JTree]
    val model = tree.getModel.asInstanceOf[DefaultTreeModel]
        // Configure for drop mode.
    var index = childIndex;    // DropMode.INSERT
    if(childIndex == -1) {     // DropMode.ON
      index = parent.getChildCount();
    }
    // Add data to model.
      model.insertNodeInto(node, parent, index)
/*     for(i <- 0 until nodes.length) {
      model.insertNodeInto(node, parent, index)
      index = index + 1
    }
 */    true
  }

  override def toString = getClass().getName()

  class NodesTransferable(val node: GuiNode) extends Transferable {
    def getTransferData(flavor: DataFlavor) = {
      if(!isDataFlavorSupported(flavor))
        throw new UnsupportedFlavorException(flavor);
      node;
    }
    def getTransferDataFlavors() = flavors
    def isDataFlavorSupported(flavor: DataFlavor) = guiNodeFlavor.equals(flavor)
  }

}
