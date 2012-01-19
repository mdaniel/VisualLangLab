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

import java.io.PrintWriter;
import net.java.vll.vll4j.api.*;
import net.java.vll.vll4j.combinator.Utils;

public class VisitorXmlGeneration extends VisitorBase {
    
    VisitorXmlGeneration(PrintWriter printWriter) {
        this.printWriter = printWriter;
    }
    
    private void commonAttributes(NodeBase n, boolean solo) {
        if (n.multiplicity != Multiplicity.One)
            printWriter.printf("Mult=\"%s\" ", n.multiplicity);
        if (!n.errorMessage.isEmpty())
            printWriter.printf("ErrMsg=\"%s\" ", Utils.encode4xml(n.errorMessage));
        if (!n.description.isEmpty())
            printWriter.printf("Description=\"%s\" ", Utils.encode4xml(n.description));
        if (!n.actionText.isEmpty())
            printWriter.printf("ActionText=\"%s\" ", Utils.encode4xml(n.actionText));
        if (n.isDropped)
            printWriter.printf("Drop=\"true\" ");
        printWriter.println(solo ? "/>" : ">");
    }
    
    private void space() {
        for (int i = 0; i < depth; ++i)
            printWriter.print("  ");
    }
    
    @Override
    public Object visitChoice(NodeChoice n) {
        space();
        printWriter.printf("<Choice ");
        commonAttributes(n, false);
        visitAllChildNodes(n);
        space();
        printWriter.println("</Choice>");
        return null;
    }
    
    @Override
    public Object visitLiteral(NodeLiteral n) {
        space();
        printWriter.printf("<Token Ref=\"%s\" ", n.literalName);
        commonAttributes(n, true);
        return null;
    }
    
    @Override
    public Object visitReference(NodeReference n) {
        space();
        printWriter.printf("<Reference Ref=\"%s\" ", n.refRuleName);
        commonAttributes(n, true);
        return null;
    }
    
    @Override
    public Object visitRegex(NodeRegex n) {
        space();
        printWriter.printf("<Token Ref=\"%s\" ", n.regexName);
        commonAttributes(n, true);
        return null;
    }
    
    @Override
    public Object visitRepSep(NodeRepSep n) {
        space();
        printWriter.printf("<RepSep ");
        commonAttributes(n, false);
        visitAllChildNodes(n);
        space();
        printWriter.println("</RepSep>");
        return null;
    }
    
    @Override
    public Object visitRoot(NodeRoot n) {
        depth = 2;
        space();
        printWriter.printf("<Parser Name=\"%s\" ", n.ruleName);
        if (n.isPackrat)
            printWriter.print("Packrat=\"true\" ");
        commonAttributes(n, false);
        visitAllChildNodes(n);
        space();
        printWriter.println("</Parser>");
        return null;
    }
    
    @Override
    public Object visitSemPred(NodeSemPred n) {
        space();
        printWriter.printf("<SemPred ");
        commonAttributes(n, true);
        return null;
    }
    
    @Override
    public Object visitSequence(NodeSequence n) {
        space();
        printWriter.printf("<Sequence ");
        commonAttributes(n, false);
        visitAllChildNodes(n);
        space();
        printWriter.println("</Sequence>");
        return null;
    }
    
    @Override
    public Object visitWildCard(NodeWildCard n) {
        space();
        printWriter.printf("<Token ");
        commonAttributes(n, true);
        return null;
    }
    
    private PrintWriter printWriter;
}
