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

package net.java.vll.vll4j;

import net.java.vll.vll4j.api.Vll4j;
import net.java.vll.vll4j.combinator.Parsers;
import net.java.vll.vll4j.gui.ReaderFile;

import java.io.File;
import java.io.FileInputStream;

public class VLL4J {

    public static void main(String args[]) {
        if (args.length != 3 && args.length != 2) {
            System.err.println("usage: java VLL4J grammar-file [parser-name] data/file");
            System.exit(1);
        }
        try {
            FileInputStream fis = new FileInputStream(args[0]);
            Vll4j vll4j = Vll4j.fromStream(fis);
            Parsers.Parser p = vll4j.getParserFor(args.length == 2 ? "Main" : args[1]);
            File dataFile = new File(args[args.length - 1]);
            Parsers.ParseResult pr = dataFile.exists() ? vll4j.parseAll(p, new ReaderFile(dataFile)) :
                    vll4j.parseAll(p, args[args.length - 1]);
            if (pr.successful())
                System.out.println(vll4j.dumpValue(pr.get(), true));
            else
                System.out.println(vll4j.dumpResult(pr));
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
