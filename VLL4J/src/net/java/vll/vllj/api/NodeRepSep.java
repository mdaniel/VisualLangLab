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

public class NodeRepSep extends NodeBase {
    
    public NodeRepSep() {
        multiplicity = Multiplicity.ZeroOrMore;
    }
    
    public String nodeType() {
        return "RepSep";
    }
    
    @Override
    public NodeRepSep clone() {
        NodeRepSep n = new NodeRepSep();
        for (int i = 0; i < getChildCount(); ++i) {
            n.add((NodeBase)(((NodeBase)getChildAt(i)).clone()));
        }
        n.copyFrom(this);
        return n;
    }

    @Override
    public Object accept(VisitorBase v) {
        return v.visitRepSep(this);
    }

    @Override
    public String getName() {
        return String.format(description.isEmpty() ? "%s %s" : "%s %s (%s)", 
                multiplicity, getAttributes(), description);
    }
        
}
