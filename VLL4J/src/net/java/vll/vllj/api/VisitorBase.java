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

package net.java.vll.vllj.api;

public abstract class VisitorBase {
    
    protected void visitAllChildNodes(NodeBase n) {
        int childCount = n.getChildCount();
        for (int i = 0; i < childCount; ++i) {
            ++depth;
            ((NodeBase)n.getChildAt(i)).accept(this);
            --depth;
        }
    }
    
    public abstract Object visitChoice(NodeChoice n);
    public abstract Object visitLiteral(NodeLiteral n);
    public abstract Object visitReference(NodeReference n);
    public abstract Object visitRegex(NodeRegex n);
    public abstract Object visitRepSep(NodeRepSep n);
    public abstract Object visitRoot(NodeRoot n);
    public abstract Object visitSemPred(NodeSemPred n);
    public abstract Object visitSequence(NodeSequence n);
    public abstract Object visitWildCard(NodeWildCard n);
    
    protected int depth = 0;
}
