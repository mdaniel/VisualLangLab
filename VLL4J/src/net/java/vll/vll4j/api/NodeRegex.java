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

package net.java.vll.vll4j.api;

public class NodeRegex extends NodeBase {

    public NodeRegex(String name) {
        this.regexName = name;
    }

    public String nodeType() {
        return "Regex";
    }
    
    @Override
    public NodeRegex clone() {
        NodeRegex n = new NodeRegex(regexName);
        n.copyFrom(this);
        return n;
    }

    @Override
    public boolean getAllowsChildren() {
        return false;
    }

    @Override
    public Object accept(VisitorBase v) {
        return v.visitRegex(this);
    }

    @Override
    public String getName() {
        return String.format(description.isEmpty() ? "%s %s %s" : "%s %s %s (%s)", 
                multiplicity, regexName, getAttributes(), description);
    }

    public String regexName;
}
