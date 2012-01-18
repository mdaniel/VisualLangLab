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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import net.java.vll.vllj.api.Vll4j;

public class TDARExprAST {

    static float evalAtom(Object ast) {
        Object pair[] = (Object[])ast;
        switch ((Integer)pair[0]) {
            case 0: 
                return Float.parseFloat((String)pair[1]);
            case 1: 
                if (memory.containsKey(pair[1])) {
                    return memory.get(pair[1]);
                }
                else {
                    System.out.printf("Undefined variable: %s%n", pair[1]); 
                    return 0;
                }
            case 2: return evalExpr(pair[1]);
        }
        return 0; /* never used, keeps compiler happy */
    }
    
    static float evalMultExpr(Object ast) {
        Object arr[] = (Object[])ast;
        Float res = evalAtom(arr[0]);
        List<Object[]> lst = (List<Object[]>)arr[1];
        for (Object pair[]: lst) {
            res *= evalAtom(pair[1]);
        }
        return res;
    }
    
    static float evalExpr(Object ast) {
        Object arr[] = (Object[])ast;
        Float res = evalMultExpr(arr[0]);
        List<Object[]> lst = (List<Object[]>)arr[1];
        for (Object pair[]: lst) {
            Object discr[] = (Object[])pair[0];
            switch ((Integer)discr[0]) {
                case 0: res += evalMultExpr(pair[1]); break;
                case 1: res -= evalMultExpr(pair[1]); break;
            }
        }
        return res;
    }
    
    static void evalStat(Object ast) {
        Object pair[] = (Object[])ast;
        switch ((Integer)pair[0]) {
            case 0: 
                Object arr[] = (Object[])pair[1];
                String id = (String)arr[0];
                Float res = evalExpr(arr[1]);
                memory.put(id, res);
                break;
            case 1: 
                System.out.println(evalExpr(pair[1])); 
                break;
            case 2: /*do nothing*/ 
                break;
        }
    }
    
    static void evalProg(Object ast) {
        List listOfStat = (List)ast;
        for (Object stat: listOfStat) {
            evalStat(stat);
        }
    }
    
    static Map<String, Float> memory = new HashMap<String, Float>();

    public static void main(String[] args) throws ParserConfigurationException, 
            SAXException, IOException {
        String input = "a=3\nb=4\n2+a*b\n";
        Vll4j vll = Vll4j.fromFile(new File("TDAR-Expr-AST.vll"));
        Vll4j.Parser exprParser = vll.getParserFor("Prog");
        Vll4j.ParseResult parseResult = vll.parseAll(exprParser, input);
        if (parseResult.successful()) {
            Object ast = parseResult.get();
            evalProg(ast);
        } else {
            System.out.println(parseResult);
        }
    }
}
