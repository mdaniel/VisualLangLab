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
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import vll.core.VllParsers;

public class TDARSimpleTreeBasedInterpreterJava {

    private static HashMap<String, BigInteger> memory = new HashMap<String, BigInteger>();
    private static HashMap<String, BigInteger> funcConstants = new HashMap<String, BigInteger>();
    private static HashMap<String, Object[]> funcFormulae = new HashMap<String, Object[]>();
    
    static BigInteger funcCall(String id, Object arg) {
        BigInteger result = BigInteger.ZERO;
        BigInteger argValue = exprHdlr(arg);
        String funcConstKey = id + ":" + argValue;
        if (funcConstants.containsKey(funcConstKey)) {
            result = funcConstants.get(funcConstKey);
        } else if (funcFormulae.containsKey(id)) {
            Object[] funcDetail = funcFormulae.get(id);
            String argName = (String) funcDetail[0];
            BigInteger oldVal = memory.containsKey(argName) ? memory.get(argName) : null;
            memory.put(argName, argValue);
            result = exprHdlr(funcDetail[1]);
            if (oldVal == null) {
                memory.remove(argName);
            } else {
                memory.put(argName, oldVal);
            }
        } else {
            System.out.printf("undefined function: %s%n", id);
        }
        return result;
    }

    static void funcHdlr(Object ast) {
        Object[] arr = (Object[]) ast;
        Object[] formalParam = (Object[]) arr[1];
        if (formalParam[0].equals(0)) {
            funcFormulae.put((String)arr[0], new Object[] {formalParam[1], arr[2]});
        } else if (formalParam[0].equals(1)) {
            funcConstants.put(arr[0] + ":" + formalParam[1], exprHdlr(arr[2]));
        }
    }
    
    static BigInteger atomHdlr(Object ast) {
        BigInteger result = BigInteger.ZERO;
        Object[] pair = (Object[]) ast;
        if (pair[0].equals(0)) {
            Object[] arr = (Object[]) pair[1];
            result = funcCall((String)arr[0], arr[1]);
        } else if (pair[0].equals(1)) {
            result = exprHdlr(pair[1]);
        } else if (pair[0].equals(2)) {
            result = BigInteger.valueOf(Long.parseLong((String)pair[1]));
        } else if (pair[0].equals(3)) {
            String id = (String)pair[1];
            if (memory.containsKey(id)) {
                result = memory.get(id);
            } else {
                System.out.printf("undefined variable: %s%n", id);
                result = BigInteger.ZERO;
            }
        }
        return result;
    }

    static BigInteger multExprHdlr(Object ast) {
        Object[] arr = (Object[]) ast;
        BigInteger result = atomHdlr(arr[0]);
        for (Object[] arr2: (List<Object[]>) arr[1]) {
            Object[] pair = (Object[]) arr2[0];
            if (pair[0].equals(0)) {
                result = result.multiply(atomHdlr(arr2[1]));
            } else if (pair[0].equals(1)) {
                result = result.divide(atomHdlr(arr2[1]));
            } else if (pair[0].equals(2)) {
                result = result.mod(atomHdlr(arr2[1]));
            }
        }
        return result;
    }

    static BigInteger exprHdlr(Object ast) {
        Object[] arr = (Object[]) ast;
        BigInteger result = multExprHdlr(arr[0]);
        for (Object[] arr2: (List<Object[]>) arr[1]) {
            Object[] pair = (Object[]) arr2[0];
            if (pair[0].equals(0)) {
                result = result.add(multExprHdlr(arr2[1]));
            } else if (pair[0].equals(1)) {
                result = result.subtract(multExprHdlr(arr2[1]));
            }
        }
        return result;
    }

    static void statHdlr(Object ast) {
        Object[] pair = (Object[]) ast;
        if (pair[0].equals(0)) {
            Object[] arr = (Object[]) pair[1];
            memory.put((String)arr[0], exprHdlr(arr[1]));
        } else if (pair[0].equals(1)) {
            funcHdlr(pair[1]);
        } else if (pair[0].equals(2)) {
            System.out.println(exprHdlr(pair[1]));
        } else if (pair[0].equals(3)) {
            // do nothing
        }
    }

    static void progHdlr(Object ast) {
        for (Object stat: (List<Object>) ast) {
            statHdlr(stat);
        }
    }

    public static void main(String[] args) {
        String input = "f(0)=1\n f(x)=f(x-1)*x\n a=3\n b=4\n (a+b)*5\n f(a+b)\n";
        VllParsers vll = VllParsers.fromFile(new File("TDAR-Simple-Tree-Based-Interpreter.vll"));
        VllParsers.Parser exprParser = vll.getParserFor("Prog");
        VllParsers.ParseResult<Object> parseResult = vll.parseAll(exprParser, input);
        if (parseResult.successful()) {
            Object ast = vll.ast4jvm(parseResult.get());
            progHdlr(ast);
        } else {
            System.out.println(parseResult);
        }
    }
}
