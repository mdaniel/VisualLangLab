/*
 Copyright 2012, Sanjay Dasgupta
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

package vll4j.gui;

import vll4j.tree.NodeBase;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

class TransferHandlerTree extends TransferHandler {

    public TransferHandlerTree() {
        try {
            dataFlavors = new DataFlavor[] {new DataFlavor(mimeType(NodeBase.class))};
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop())
            return false;
        support.setShowDropLocation(true);
        if(!support.isDataFlavorSupported(dataFlavors[0]))
            return false;
        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        Object par1 = dl.getPath().getLastPathComponent();
//System.out.printf("canImport(==?): %s == %s%n", par1, nodeToRemove.getParent());
        if ((nodeToRemove == null) || (nodeToRemove.getParent() == null) || par1 != nodeToRemove.getParent()) {
            return false;
        }
        return true;
    }
    
    @Override
    public Transferable createTransferable(JComponent c) {
        TreePath path = ((JTree)c).getSelectionPath();
        if (path != null) {
            final NodeBase node = (NodeBase)path.getLastPathComponent();
            nodeToRemove = node;
            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return dataFlavors;
                }
                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return dataFlavors[0].equals(flavor);
                }
                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                    if(!isDataFlavorSupported(flavor))
                        throw new UnsupportedFlavorException(flavor);
                    else
                        return node.clone();
                }
            };
        } else {
            return null;
        }
    }
    
    public void exportDone(JComponent source, Transferable data, int action) {
//System.out.println("exportDone()");
        if((action & MOVE) == MOVE) {
            DefaultTreeModel model = (DefaultTreeModel)((JTree)source).getModel();
            model.removeNodeFromParent(nodeToRemove);
        }
    }
    
    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.MOVE;
    }
    
    public boolean importData(TransferHandler.TransferSupport support) {
//System.out.println("importData()");
        if(!canImport(support))
            return false;
        // Extract transfer data.
        NodeBase node = null;
        try {
            Transferable t = support.getTransferable();
            node = (NodeBase)t.getTransferData(dataFlavors[0]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // Get drop location info.
        JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        NodeBase parent = (NodeBase)dest.getLastPathComponent();
        JTree tree = (JTree)support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
    // Add data to model.
        model.insertNodeInto(node, parent, index);
        return true;
    }

    private String mimeType(Class c) {
        String mt = String.format("%s;class=\"%s\"", DataFlavor.javaJVMLocalObjectMimeType, c.getName());
        return mt;
    }
    
    NodeBase nodeToRemove = null;
    DataFlavor dataFlavors[] = null;
}
