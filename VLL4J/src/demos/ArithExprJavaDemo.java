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

package demos;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import vll4j.Vll4j;

public class ArithExprJavaDemo {

    static Float evalFactorAST(Object ast) {
        Object[] pair = (Object[]) ast;
        Float factorResult = -1f;
        if (pair[0].equals(0)) {
            factorResult = Float.parseFloat((String) pair[1]);
        } else if (pair[0].equals(1)) {
            factorResult = evalExprAST(pair[1]);
        }
        return factorResult;
    }

    static Float evalExprAST(Object ast) {
        Object[] arr = (Object[]) ast;
        Float term = evalTermAST(arr[0]);
        for (Object pair[] : (List<Object[]>) arr[1]) {
            if (pair[0].equals(0)) {
                term += evalTermAST(pair[1]);
            } else if (pair[0].equals(1)) {
                term -= evalTermAST(pair[1]);
            }
        }
        return term;
    }

    static Float evalTermAST(Object ast) {
        Object[] arr = (Object[]) ast;
        Float factor = evalFactorAST(arr[0]);
        for (Object[] pair : (List<Object[]>) arr[1]) {
            if (pair[0].equals(0)) {
                factor *= evalFactorAST(pair[1]);
            } else if (pair[0].equals(1)) {
                factor /= evalFactorAST(pair[1]);
            }
        }
        return factor;
    }

    public static void main(String[] args) throws ParserConfigurationException, 
            SAXException, IOException {
        String input = "(3 + 5) * (8 - 4)";
        Vll4j vll = Vll4j.fromFile(new File("ArithExpr.vll"));
        Vll4j.Parser exprParser = vll.getParser("Expr");
        Vll4j.ParseResult parseResult = vll.parseAll(exprParser, input);
        if (parseResult.successful()) {
            Object ast = parseResult.get();
            Float result = evalExprAST(ast);
            System.out.println(result);
        } else {
            System.out.println(parseResult);
        }
    }
}
