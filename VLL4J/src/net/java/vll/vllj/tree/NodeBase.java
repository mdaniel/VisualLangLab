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

package net.java.vll.vllj.tree;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class NodeBase extends DefaultMutableTreeNode {

    public abstract Object accept(VisitorBase vb);
    public abstract String getName();
    public abstract String nodeType();
    
    protected void copyFrom(NodeBase src) {
        multiplicity = src.multiplicity;
        errorMessage = src.errorMessage;
        description = src.description;
        actionText = src.actionText;
        isTraced = false;
        isDropped = src.isDropped;
        actionFunction = src.actionFunction;
    }

    public String nodeName() {
        NodeBase parentNode = (NodeBase)getParent();
        if (parentNode == null)
            return "";
        int idx = parentNode.getIndex(this);
        return String.format("%s.%d", parentNode.nodeName(), idx);
    }

    public String getAttributes() {
        if (isTraced || isDropped || !errorMessage.trim().isEmpty() || !actionText.trim().isEmpty() || 
                (this instanceof NodeRoot && (((NodeRoot)this).isPackrat))) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (!actionText.trim().isEmpty())
                sb.append("action");
            if (isDropped) {
                if (sb.length() != 1)
                    sb.append(" ");
                sb.append("drop");
            }
            if (!errorMessage.trim().isEmpty()) {
                if (sb.length() != 1)
                    sb.append(" ");
                sb.append("message");
            }
            if ((this instanceof NodeRoot && (((NodeRoot)this).isPackrat))) {
                if (sb.length() != 1)
                    sb.append(" ");
                sb.append("packrat");
            }
            if (isTraced) {
                if (sb.length() != 1)
                    sb.append(" ");
                sb.append("trace");
            }
            sb.append("]");
            return sb.toString();  
        } else {
            return "";
        }
    }

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + nodeName();
    }

    public Multiplicity multiplicity = Multiplicity.One;
    public String errorMessage = "";
    public String description = "";
    public String actionText = "";
    public boolean isTraced = false;
    public boolean isDropped = false;
    public ActionFunction actionFunction = null;
}
