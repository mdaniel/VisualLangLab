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

package net.java.vll.vll4j.gui;

import java.util.*;
import net.java.vll.vll4j.api.*;

public class VisitorAstDescription extends VisitorBase {
    
    public VisitorAstDescription(Vll4jGui gui) {
        this.gui = gui;
    }
    
    @Override
    public Object visitChoice(NodeChoice n) {
        if (level > maxDepth)
            return getSpaces(n) + "_";
        if (n != gui.theTreePanel.selectedNode && !n.actionText.trim().isEmpty())
            return String.format("%saction@%s", getSpaces(n), n.nodeName());
        if (n.getChildCount() == 0)
            return getSpaces(n) + "?";
        StringBuilder sb = new StringBuilder();
        sb.append(getSpaces(n)).append("Choice(\n");
        ++level;
        int cc = n.getChildCount();
        for (int i = 0; i < cc; ++i) {
            sb.append(getSpaces(n)).append("Array(").append(i);
            ++level;
            String childAst = (String)((NodeBase)n.getChildAt(i)).accept(this);
            if (childAst.contains("\n")) {
                sb.append(",\n").append(childAst).append("\n");
                --level;
                sb.append(getSpaces(n)).append(")");
            } else {
                sb.append(", ").append(stripSpaces(childAst)).append(")");
                --level;
            }
            if (i == cc - 1)
                sb.append("\n");
            else
                sb.append(",\n");
        }
        --level;
        sb.append(getSpaces(n)).append(")");
        return withMultiplicity(sb.toString(), n);
    }
    
    @Override
    public Object visitLiteral(NodeLiteral n) {
        if (level > maxDepth)
            return getSpaces(n) + "_";
        if (n != gui.theTreePanel.selectedNode && !n.actionText.trim().isEmpty())
            return String.format("%saction@%s", getSpaces(n), n.nodeName());
        StringBuilder sb = new StringBuilder();
        sb.append(getSpaces(n)).append("\"").
                append((String)gui.theForest.tokenBank.get(n.literalName).substring(1)).append("\"");
        return withMultiplicity(sb.toString(), n);
    }
    
    @Override
    public Object visitReference(NodeReference n) {
        if (level > maxDepth)
            return getSpaces(n) + "_";
        if (n != gui.theTreePanel.selectedNode && !n.actionText.trim().isEmpty())
            return String.format("%saction@%s", getSpaces(n), n.nodeName());
        if (maxDepth == Integer.MAX_VALUE) {
//            ++level;
            String ast = withMultiplicity((String)gui.theForest.ruleBank.get(n.refRuleName).accept(this), n);
//            --level;
            return ast;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(getSpaces(n)).append("@").append(n.refRuleName);
            return withMultiplicity(sb.toString(), n);
        }
    }
    
    @Override
    public Object visitRegex(NodeRegex n) {
        if (level > maxDepth)
            return getSpaces(n) + "_";
        if (n != gui.theTreePanel.selectedNode && !n.actionText.trim().isEmpty())
            return String.format("%saction@%s", getSpaces(n), n.nodeName());
        StringBuilder sb = new StringBuilder();
        sb.append(getSpaces(n)).append("[").append(n.regexName).append("]");
        return withMultiplicity(sb.toString(), n);
    }
    
    @Override
    public Object visitRepSep(NodeRepSep n) {
        if (level > maxDepth)
            return getSpaces(n) + "_";
        if (n.getChildCount() == 0)
            return getSpaces(n) + "?";
        if (n != gui.theTreePanel.selectedNode && !n.actionText.trim().isEmpty())
            return String.format("%saction@%s", getSpaces(n), n.nodeName());
        return withMultiplicity((String)((NodeBase)n.getChildAt(0)).accept(this), n);
    }
    
    @Override
    public Object visitRoot(NodeRoot n) {
        if (n.getChildCount() == 0)
            return getSpaces(n) + "?";
        if (n != gui.theTreePanel.selectedNode && !n.actionText.trim().isEmpty())
            return String.format("%saction@%s", getSpaces(n), n.nodeName());
        if (activeNodes.contains(n)) {
            return getSpaces(n) + "_";
        } else {
            activeNodes.add(n);
        }
//        ++level;
        String ast = (String)((NodeBase)n.getChildAt(0)).accept(this);
//        --level;
        activeNodes.remove(n);
        return ast;
    }
    
    @Override
    public Object visitSemPred(NodeSemPred n) {
        // Never called! Exists only to keep the compiler happy
        return getSpaces(n) + "_";
    }
    
    @Override
    public Object visitSequence(NodeSequence n) {
        if (level > maxDepth)
            return getSpaces(n) + "_";
        if (n != gui.theTreePanel.selectedNode && !n.actionText.trim().isEmpty())
            return String.format("%saction@%s", getSpaces(n), n.nodeName());
        if (n.getChildCount() == 0)
            return getSpaces(n) + "?";
        ArrayList<NodeBase> childNodes = childNodesInAST(n);
        int cc = childNodes.size();
        StringBuilder sb = new StringBuilder();
        if (cc == 0) {
            sb.append(getSpaces(n)).append("Array()");
            return sb.toString();
        } else if (cc == 1) {
            sb.append(childNodes.get(0).accept(this));
            return withMultiplicity(sb.toString(), n);
        } else {
            sb.append(getSpaces(n)).append("Array(\n");
            ++level;
            for (int i = 0; i < cc; ++i) {
                sb.append(childNodes.get(i).accept(this));
                if (i == cc - 1)
                    sb.append("\n");
                else
                    sb.append(",\n");
            }
            --level;
            sb.append(getSpaces(n)).append(")");
            return withMultiplicity(sb.toString(), n);
        }
    }
    
    private ArrayList<NodeBase> childNodesInAST(NodeSequence ns) {
        ArrayList<NodeBase> al = new ArrayList<NodeBase>();
        for (int i = 0; i < ns.getChildCount(); ++i) {
            NodeBase nc = (NodeBase)ns.getChildAt(i);
            if (nc.multiplicity == Multiplicity.Guard || nc.multiplicity == Multiplicity.Not || 
                    nc.isDropped || nc instanceof NodeSemPred)
                continue;
            al.add(nc);
        }
        return al;
    }
    
    private String withMultiplicity(String ast, NodeBase n) {
        StringBuilder sb = new StringBuilder();
        if (n.multiplicity == Multiplicity.ZeroOrOne) {
            if (ast.contains("\n")) {
                sb.append(getSpaces(n)).append("Option(\n");
                sb.append(pad(ast)).append("\n");
                sb.append(getSpaces(n)).append(")");
            } else {
                sb.append(getSpaces(n)).append("Option(").append(stripSpaces(ast)).append(")");
            }
        } else if (n.multiplicity == Multiplicity.OneOrMore || 
                n.multiplicity == Multiplicity.ZeroOrMore) {
            if (ast.contains("\n")) {
                sb.append(getSpaces(n)).append("List(\n");
                sb.append(pad(ast)).append("\n");
                sb.append(getSpaces(n)).append(")");
            } else {
                sb.append(getSpaces(n)).append("List(").append(stripSpaces(ast)).append(")");
            }
        } else {
            sb.append(ast);
        }
        if (!n.description.isEmpty())
            sb.append(" <").append(n.description).append(">");
        return sb.toString();
    }
    
    private String getSpaces(NodeBase n) {
        if (level == 0)
            return "";
        else if (level == 1)
            return spacer;
        else if (spacers.containsKey(level))
            return spacers.get(level);
        else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < level; ++i)
                sb.append(spacer);
            spacers.put(level, sb.toString());
            return sb.toString();
        }
    }
    
    private String stripSpaces(String a) {
        if (a.startsWith(spacer))
            return stripSpaces(a.substring(spacer.length()));
        else
            return a;
    }
    
    private String pad(String a) {
        StringBuilder sb = new StringBuilder();
        String lines[] = a.split("\n");
        boolean first = true;
        for (String s: lines) {
            if (first) {
                first = false;
            } else {
                sb.append("\n");
            }
            sb.append(spacer).append(s);
        }
        return sb.toString();
    }
    
    @Override
    public Object visitWildCard(NodeWildCard n) {
        // Never called! Exists only to keep the compiler happy
        return getSpaces(n) + "*";
    }
    
    private Vll4jGui gui;
    private String spacer  = "|  ";
    private Map<Integer, String> spacers = new HashMap<Integer, String>();
    int level = 0;
    int maxDepth = 0;
    private Set<NodeBase> activeNodes = new HashSet<NodeBase>();
}
