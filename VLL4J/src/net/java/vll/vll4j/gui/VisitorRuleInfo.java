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

import net.java.vll.vll4j.api.*;

public class VisitorRuleInfo extends VisitorBase {
    
    @Override
    public Object visitChoice(NodeChoice n) {
        if (!n.actionText.trim().isEmpty())
            hasActions = true;
        if (visitorValidation.visitChoice(n) != null)
            hasErrors = true;
        visitAllChildNodes(n);
        return null;
    }
    
    @Override
    public Object visitLiteral(NodeLiteral n) {
        if (!n.actionText.trim().isEmpty())
            hasActions = true;
        if (visitorValidation.visitLiteral(n) != null)
            hasErrors = true;
        return null;
    }
    
    @Override
    public Object visitReference(NodeReference n) {
        if (!n.actionText.trim().isEmpty())
            hasActions = true;
        if (visitorValidation.visitReference(n) != null)
            hasErrors = true;
        return null;
    }
    
    @Override
    public Object visitRegex(NodeRegex n) {
        if (!n.actionText.trim().isEmpty())
            hasActions = true;
        if (visitorValidation.visitRegex(n) != null)
            hasErrors = true;
        return null;
    }
    
    @Override
    public Object visitRepSep(NodeRepSep n) {
        if (!n.actionText.trim().isEmpty())
            hasActions = true;
        if (visitorValidation.visitRepSep(n) != null)
            hasErrors = true;
        visitAllChildNodes(n);
        return null;
    }
    
    @Override
    public Object visitRoot(NodeRoot n) {
        hasErrors = hasActions = isTester = false;
        if (n != null) {
            if (!n.actionText.trim().isEmpty())
                hasActions = true;
            if (visitorValidation.visitRoot(n) != null)
                hasErrors = true;
            if (n.getChildCount() != 0) {
                NodeBase c = (NodeBase)n.getChildAt(0);
                String action = c.actionText.trim();
                if (!action.isEmpty()) {
                    if (action.contains("vllParserTestInput") || action.contains("vllParserLog"))
                        isTester = true;
                }
            }
            visitAllChildNodes(n);
        }
        return null;
    }
    
    @Override
    public Object visitSemPred(NodeSemPred n) {
        if (visitorValidation.visitSemPred(n) != null)
            hasErrors = true;
        return null;
    }
    
    @Override
    public Object visitSequence(NodeSequence n) {
        if (!n.actionText.trim().isEmpty())
            hasActions = true;
        if (visitorValidation.visitSequence(n) != null)
            hasErrors = true;
        visitAllChildNodes(n);
        return null;
    }
    
    @Override
    public Object visitWildCard(NodeWildCard n) {
        if (!n.actionText.trim().isEmpty())
            hasActions = true;
        if (visitorValidation.visitWildCard(n) != null)
            hasErrors = true;
        return null;
    }
    
    public boolean hasErrors = false, hasActions = false, isTester;
    private VisitorValidation visitorValidation = new VisitorValidation();
}
