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

package vll4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import vll4j.core.Parsers.ParseResult;
import vll4j.core.Parsers.Parser;
import vll4j.core.ReaderFile;
import vll4j.core.SimpleLexingParsers;
import vll4j.tree.Forest;
import vll4j.tree.NodeBase;
import vll4j.tree.VisitorParserGeneration;

public class VLL4J {
    
    public VLL4J(InputStream is) throws ParserConfigurationException, SAXException, IOException {
        forest.openInputStream(is);
    }
    
    public <T> Parser<T> getParser(String parserName) throws ParserConfigurationException, SAXException, IOException {
        slp.reset();
        slp.commentSpecRegex = forest.comment;
        slp.whiteSpaceRegex = forest.whiteSpace;
        slp.resetWhitespace();
        VisitorParserGeneration vpg = new VisitorParserGeneration(forest, slp, false);
        NodeBase top = forest.ruleBank.get(parserName);
        return (Parser<T>) top.accept(vpg);
    }
    
    public static void main(String args[]) throws Exception {
        if (args.length < 2 || args.length > 3) {
            System.err.println("usage: java vll4j.VLL4J grammar-file data-file [parser-name]");
            System.exit(1);
        }
        FileInputStream fis = new FileInputStream(args[0]);
        VLL4J vll4j = new VLL4J(fis);
        Parser p = vll4j.getParser(args.length == 2 ? "Main" : args[2]);
        ParseResult pr = vll4j.slp.parseAll(p, new ReaderFile(new File(args[1])));
        if (pr.successful())
            System.out.println(vll4j.slp.dumpValue(pr.get()));
        else
            System.out.println(vll4j.slp.dumpResult(pr));
    }
    
    private SimpleLexingParsers slp = new SimpleLexingParsers();
    private Forest forest = new Forest();
}
