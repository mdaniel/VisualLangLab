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

public class NodeLiteral extends NodeBase {

    public NodeLiteral(String name) {
        this.literalName = name;
    }

    public String nodeType() {
        return "Literal";
    }
    
    @Override
    public NodeLiteral clone() {
        NodeLiteral n = new NodeLiteral(literalName);
        n.copyFrom(this);
        return n;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public Object accept(VisitorBase v) {
        return v.visitLiteral(this);
    }

    @Override
    public String getName() {
        return String.format(description.isEmpty() ? "%s %s %s" : "%s %s %s (%s)", 
                multiplicity, literalName, getAttributes(), description);
    }

    public String literalName;
}
