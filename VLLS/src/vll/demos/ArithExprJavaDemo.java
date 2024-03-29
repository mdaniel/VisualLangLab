/*
Copyright 2010, Sanjay Dasgupta
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
package vll.demos;

import java.io.File;
import java.util.List;
import vll.core.VllParsers;

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
        Float exprResult = evalTermAST(arr[0]);
        for (Object[] pair : (List<Object[]>) arr[1]) {
            if (pair[0].equals(0)) {
                exprResult += evalTermAST(pair[1]);
            } else if (pair[0].equals(1)) {
                exprResult -= evalTermAST(pair[1]);
            }
        }
        return exprResult;
    }

    static Float evalTermAST(Object ast) {
        Object[] arr = (Object[]) ast;
        Float termResult = evalFactorAST(arr[0]);
        for (Object[] pair : (List<Object[]>) arr[1]) {
            if (pair[0].equals(0)) {
                termResult *= evalFactorAST(pair[1]);
            } else if (pair[0].equals(1)) {
                termResult /= evalFactorAST(pair[1]);
            }
        }
        return termResult;
    }

    public static void main(String[] args) {
        String input = "(3 + 5) * (8 - 4)";
        VllParsers vll = VllParsers.fromFile(new File("ArithExpr.vll"));
        VllParsers.Parser exprParser = vll.getParserFor("Expr");
        VllParsers.ParseResult<Object> parseResult = vll.parseAll(exprParser, input);
        if (parseResult.successful()) {
            Object ast = vll.ast4jvm(parseResult.get());
            Float result = evalExprAST(ast);
            System.out.println(result);
        } else {
            System.out.println(parseResult);
        }
    }
}
