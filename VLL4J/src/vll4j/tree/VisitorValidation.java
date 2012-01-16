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

package vll4j.tree;

public class VisitorValidation extends VisitorBase {
    
    private String checkAction (NodeBase n) {
        if (n.actionText.trim().isEmpty() || n.actionFunction != null)
            return null;
        else 
            return "error in action-code";
    }
    
    @Override
    public String visitChoice(NodeChoice n) {
        if (n.getChildCount() >= 2) {
            return checkAction(n);
        } else {
            return checkAction(n) == null ? "needs 2 or more child nodes" 
                    : "needs 2 or more child nodes, " + checkAction(n);
        }
    }
    
    @Override
    public String visitLiteral(NodeLiteral n) {
        return checkAction(n);
    }
    
    @Override
    public String visitReference(NodeReference n) {
        return checkAction(n);
    }
    
    @Override
    public String visitRegex(NodeRegex n) {
        return checkAction(n);
    }
    
    @Override
    public String visitRepSep(NodeRepSep n) {
        if (n.getChildCount() == 2) {
            return checkAction(n);
        } else {
            return checkAction(n) == null ? "needs 2 child nodes" 
                    : "needs 2 child nodes, " + checkAction(n);
        }
    }
    
    @Override
    public String visitRoot(NodeRoot n) {
        if (n.getChildCount() == 1) {
            return checkAction(n);
        } else {
            return checkAction(n) == null ? "needs 1 child node" 
                    : "needs 1 child node, " + checkAction(n);
        }
    }
    
    @Override
    public String visitSemPred(NodeSemPred n) {
        if (n.actionText.trim().isEmpty())
            return "no predicate code";
        else
            return checkAction(n);
    }
    
    @Override
    public String visitSequence(NodeSequence n) {
        if (n.getChildCount() > 0) {
            return checkAction(n);
        } else {
            return checkAction(n) == null ? "needs 1 or more child nodes" 
                    : "needs 1 or more child nodes, " + checkAction(n);
        }
    }
    
    @Override
    public String visitWildCard(NodeWildCard n) {
        return null;
    }
    
}
