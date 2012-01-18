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

package net.java.vll.vllj.gui;

import net.java.vll.vllj.api.NodeRoot;
import net.java.vll.vllj.api.VisitorBase;
import net.java.vll.vllj.api.NodeSemPred;
import net.java.vll.vllj.api.NodeWildCard;
import net.java.vll.vllj.api.NodeLiteral;
import net.java.vll.vllj.api.NodeBase;
import net.java.vll.vllj.api.NodeChoice;
import net.java.vll.vllj.api.NodeRegex;
import net.java.vll.vllj.api.NodeSequence;
import net.java.vll.vllj.api.NodeRepSep;
import net.java.vll.vllj.api.NodeReference;
import java.util.TreeSet;

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
    
    @Override
    public Object visitWildCard(NodeWildCard n) {
        return null;
    }
    
    TreeSet<String> ruleSet = new TreeSet<String>();
    private String tokenToFind;
}
