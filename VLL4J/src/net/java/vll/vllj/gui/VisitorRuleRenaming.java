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

import net.java.vll.vllj.tree.NodeRoot;
import net.java.vll.vllj.tree.VisitorBase;
import net.java.vll.vllj.tree.NodeSemPred;
import net.java.vll.vllj.tree.NodeWildCard;
import net.java.vll.vllj.tree.NodeLiteral;
import net.java.vll.vllj.tree.NodeChoice;
import net.java.vll.vllj.tree.NodeRegex;
import net.java.vll.vllj.tree.NodeSequence;
import net.java.vll.vllj.tree.NodeRepSep;
import net.java.vll.vllj.tree.NodeReference;
import java.util.ArrayList;

public class VisitorRuleRenaming extends VisitorBase {
    
    public VisitorRuleRenaming(String currentName, String newName) {
        this.currentName = currentName;
        this.newName = newName;
    }
    
    @Override
    public Object visitChoice(NodeChoice n) {
        visitAllChildNodes(n);
        return null;
    }
    
    @Override
    public Object visitLiteral(NodeLiteral n) {
        return null;
    }
    
    @Override
    public Object visitReference(NodeReference n) {
        if (n.refRuleName.equals(currentName))
            n.refRuleName = newName;
        return null;
    }
    
    @Override
    public Object visitRegex(NodeRegex n) {
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
        if (n.ruleName.equals(currentName))
            n.ruleName = newName;
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
    
    ArrayList<String> ruleList = new ArrayList<String>();
    private String currentName, newName;
}
