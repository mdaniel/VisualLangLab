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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import vll4j.tree.Multiplicity;
import vll4j.tree.NodeBase;
import vll4j.tree.NodeChoice;
import vll4j.tree.NodeLiteral;
import vll4j.tree.NodeReference;
import vll4j.tree.NodeRegex;
import vll4j.tree.NodeRepSep;
import vll4j.tree.NodeRoot;
import vll4j.tree.NodeSemPred;
import vll4j.tree.NodeSequence;
import vll4j.tree.VisitorBase;
import vll4j.tree.VisitorValidation;

public class VisitorAstGeneration extends VisitorBase {
    
    public VisitorAstGeneration(Vll4jGui gui) {
        this.gui = gui;
    }
    
    @Override
    public Object visitChoice(NodeChoice n) {
        if (level > theDepth)
            return getSpaces() + "_";
        StringBuilder sb = new StringBuilder();
        sb.append(getSpaces()).append("Choice(\n");
        ++level;
        int cc = n.getChildCount();
        for (int i = 0; i < cc; ++i) {
            sb.append(getSpaces()).append("Array(").append(i);
            ++level;
            String childAst = (String)((NodeBase)n.getChildAt(i)).accept(this);
            if (childAst.contains("\n")) {
                sb.append(",\n").append(childAst).append("\n");
                --level;
                sb.append(getSpaces()).append(")");
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
        sb.append(getSpaces()).append(")");
        return withMultiplicity(sb.toString(), n);
    }
    
    @Override
    public Object visitLiteral(NodeLiteral n) {
        if (level > theDepth)
            return getSpaces() + "_";
        StringBuilder sb = new StringBuilder();
        sb.append(getSpaces()).append("\"").
                append((String)gui.theForest.tokenBank.get(n.literalName).substring(1)).append("\"");
        return withMultiplicity(sb.toString(), n);
    }
    
    @Override
    public Object visitReference(NodeReference n) {
        if (level > theDepth)
            return getSpaces() + "_";
        if (theDepth == Integer.MAX_VALUE) {
//            ++level;
            String ast = withMultiplicity((String)gui.theForest.ruleBank.get(n.refRuleName).accept(this), n);
//            --level;
            return ast;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(getSpaces()).append("@").append(n.refRuleName);
            return withMultiplicity(sb.toString(), n);
        }
    }
    
    @Override
    public Object visitRegex(NodeRegex n) {
        if (level > theDepth)
            return getSpaces() + "_";
        StringBuilder sb = new StringBuilder();
        sb.append(getSpaces()).append("[").append(n.regexName).append("]");
        return withMultiplicity(sb.toString(), n);
    }
    
    @Override
    public Object visitRepSep(NodeRepSep n) {
        if (level > theDepth)
            return getSpaces() + "_";
        if (n.getChildCount() == 2) {
            return withMultiplicity((String)((NodeBase)n.getChildAt(0)).accept(this), n);
        } else {
            return "ERROR: " + (String)n.accept(visitorNodeValidation);
        }
    }
    
    @Override
    public Object visitRoot(NodeRoot n) {
        if (level > theDepth)
            return getSpaces() + "_";
        if (activeNodes.contains(n)) {
            return getSpaces() + "_";
        } else {
            activeNodes.add(n);
        }
        if (n.getChildCount() == 1) {
            String ast = (String)((NodeBase)n.getChildAt(0)).accept(this);
            activeNodes.remove(n);
            return ast;
        } else {
            activeNodes.remove(n);
            return "ERROR: " + (String)n.accept(visitorNodeValidation);
        }
    }
    
    @Override
    public Object visitSemPred(NodeSemPred n) {
        // Never called! Exists only to keep the compiler happy
        return getSpaces() + "_";
    }
    
    @Override
    public Object visitSequence(NodeSequence n) {
        if (level > theDepth)
            return getSpaces() + "_";
        ArrayList<NodeBase> childNodes = childNodesInAST(n);
        int cc = childNodes.size();
        StringBuilder sb = new StringBuilder();
        if (cc == 0) {
            sb.append(getSpaces()).append("Array()");
            return sb.toString();
        } else if (cc == 1) {
            sb.append(childNodes.get(0).accept(this));
            return withMultiplicity(sb.toString(), n);
        } else {
            sb.append(getSpaces()).append("Array(\n");
            ++level;
            for (int i = 0; i < cc; ++i) {
                sb.append(childNodes.get(i).accept(this));
                if (i == cc - 1)
                    sb.append("\n");
                else
                    sb.append(",\n");
            }
            --level;
            sb.append(getSpaces()).append(")");
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
                sb.append(getSpaces()).append("ArOpt(\n");
                sb.append(pad(ast)).append("\n");
                sb.append(getSpaces()).append(")");
            } else {
                sb.append(getSpaces()).append("ArOpt(").append(stripSpaces(ast)).append(")");
            }
        } else if (n.multiplicity == Multiplicity.OneOrMore || 
                n.multiplicity == Multiplicity.ZeroOrMore) {
            if (ast.contains("\n")) {
                sb.append(getSpaces()).append("List(\n");
                sb.append(pad(ast)).append("\n");
                sb.append(getSpaces()).append(")");
            } else {
                sb.append(getSpaces()).append("List(").append(stripSpaces(ast)).append(")");
            }
        } else {
            sb.append(ast);
        }
        if (!n.description.isEmpty())
            sb.append(" <").append(n.description).append(">");
        return sb.toString();
    }
    
    private String getSpaces() {
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
    
    private Vll4jGui gui;
    private String spacer  = "|  ";
    private Map<Integer, String> spacers = new HashMap<Integer, String>();
    int level = 0;
    private VisitorValidation visitorNodeValidation = new VisitorValidation();
    int theDepth = 0;
    private Set<NodeBase> activeNodes = new HashSet<NodeBase>();
}
