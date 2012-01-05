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

import java.util.TreeSet;
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

public class VisitorTokenSearch extends VisitorBase {
    
    public VisitorTokenSearch(String tokenToFind) {
        super();
        this.tokenToFind = tokenToFind;
    }
    
    private String getRuleName(NodeBase node) {
        while (!(node instanceof NodeRoot)) {
            node = (NodeBase)node.getParent();
        }
        return ((NodeRoot)node).ruleName;
    }
    
    @Override
    public Object visitChoice(NodeChoice n) {
        visitAllChildNodes(n);
        return null;
    }
    
    @Override
    public Object visitLiteral(NodeLiteral n) {
        if (n.literalName.equals(tokenToFind))
            ruleSet.add(getRuleName(n));
        return null;
    }
    
    @Override
    public Object visitReference(NodeReference n) {
        return null;
    }
    
    @Override
    public Object visitRegex(NodeRegex n) {
        if (n.regexName.equals(tokenToFind))
            ruleSet.add(getRuleName(n));
        return null;
    }
    
    @Override
    public Object visitRepSep(NodeRepSep n) {
        visitAllChildNodes(n);
        return null;
    }
    
    @Override
    public Object visitRoot(NodeRoot n) {
        visitAllChildNodes(n);
        return null;
    }
    
    @Override
    public Object visitSemPred(NodeSemPred n) {
        return null;
    }
    
    @Override
    public Object visitSequence(NodeSequence n) {
        visitAllChildNodes(n);
        return null;
    }
    
    TreeSet<String> ruleSet = new TreeSet<String>();
    private String tokenToFind;
}