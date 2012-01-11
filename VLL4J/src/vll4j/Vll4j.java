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

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import vll4j.core.PackratParsers;
import vll4j.core.Parsers.ParseResult;
import vll4j.core.Parsers.Parser;
import vll4j.core.ReaderFile;
import vll4j.tree.Forest;
import vll4j.tree.NodeBase;
import vll4j.tree.VisitorParserGeneration;

public class Vll4j extends PackratParsers {

    public static Vll4j fromStream(InputStream is) throws ParserConfigurationException, 
            SAXException, IOException {
        Vll4j vll4j = new Vll4j();
        vll4j.initForest(is);
        return vll4j;
    }

    public static Vll4j fromString(String inString) throws ParserConfigurationException, 
            SAXException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(inString.getBytes());
        Vll4j vll4j = new Vll4j();
        vll4j.initForest(bais);
        return vll4j;
    }

    public static Vll4j fromFile(File inFile) throws ParserConfigurationException, 
            SAXException, IOException {
        FileInputStream fis = new FileInputStream(inFile);
        Vll4j vll4j = new Vll4j();
        vll4j.initForest(fis);
        return vll4j;
    }

    public <T> Parser<T> getParserFor(String ruleName) throws ParserConfigurationException, 
            SAXException, IOException {
        if (!forest.ruleBank.containsKey(ruleName))
            throw new IllegalArgumentException(String.format("unknown rule: %s", ruleName));
        reset();
        VisitorParserGeneration vpg = new VisitorParserGeneration(forest, this, false);
        NodeBase top = forest.ruleBank.get(ruleName);
        return (Parser<T>) top.accept(vpg);
    }

    public static void main(String args[]) {
        if (args.length != 3 && args.length != 2) {
            System.err.println("usage: java vll4j.Vll4j grammar [parser-name] file/data");
            System.exit(1);
        }
        try {
            FileInputStream fis = new FileInputStream(args[0]);
            Vll4j vll4j = Vll4j.fromStream(fis);
            Parser p = vll4j.getParserFor(args.length == 2 ? "Main" : args[1]);
            File dataFile = new File(args[args.length - 1]);
            ParseResult pr = dataFile.exists() ? vll4j.parseAll(p, new ReaderFile(dataFile)) : 
                    vll4j.parseAll(p, args[args.length - 1]);
            if (pr.successful())
                System.out.println(vll4j.dumpValue(pr.get()));
            else
                System.out.println(vll4j.dumpResult(pr));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void initForest(InputStream fis) throws ParserConfigurationException, 
            SAXException, IOException {
        forest.openInputStream(fis);
        commentSpecRegex = forest.comment;
        whiteSpaceRegex = forest.whiteSpace;
        resetWhitespace();
    }

    private Forest forest = new Forest();
}
