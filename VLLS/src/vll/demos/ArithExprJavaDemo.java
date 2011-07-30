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
import scala.Tuple2;
import scala.collection.immutable.List;
import scala.util.parsing.input.CharSequenceReader;
import vll.core.VllParsers;

public class ArithExprJavaDemo {
    
  static Float evalFactorAST(Object ast) {
      Tuple2<Integer, Object> pair = (Tuple2<Integer, Object>) ast;
      Float result = -1f;
      if (pair._1 . equals(new Integer(0))) {
          result =  Float.parseFloat((String)pair._2);
      } else if (pair._1 . equals(new Integer(1))) {
          result = evalExprAST(pair._2);
      }
      return result;
  }
  
    static Float evalExprAST(Object ast) {
        Object[] arr = (Object[]) ast;
        Float result = evalTermAST(arr[0]);
        List<Object> aList = (List<Object>) arr[1];
        for (int i = 0; i < aList.length(); ++i) {
           Tuple2<Integer, Object> pair = (Tuple2<Integer, Object>) aList.apply(i);
          if (pair._1 . equals(new Integer(0))) {
              result += evalTermAST(pair._2);
          } else if (pair._1 . equals(new Integer(1))) {
              result -= evalTermAST(pair._2);
          }
        }
      return result;
  }
  
  static Float evalTermAST(Object ast) {
        Object[] arr = (Object[]) ast;
        Float result = evalFactorAST(arr[0]);
        List<Object> aList = (List<Object>) arr[1];
        for (int i = 0; i < aList.length(); ++i) {
           Tuple2<Integer, Object> pair = (Tuple2<Integer, Object>) aList.apply(i);
          if (pair._1 . equals(new Integer(0))) {
              result *= evalFactorAST(pair._2);
          } else if (pair._1 . equals(new Integer(1))) {
              result /= evalFactorAST(pair._2);
          }
        }
      return result;
  }
  
    public static void main(String[] args) {
        String input = "(3 + 5) * (8 - 4)";
        VllParsers vll = VllParsers.fromFile(new File("ArithExpr.vll"));
        VllParsers.Parser phraseParser = vll.phrase(vll.getParserFor("Expr"));
        VllParsers.ParseResult<Object> parseResult = phraseParser.apply(new CharSequenceReader(input));
        if (parseResult.successful()) {
            Object ast = parseResult.get();
            Float result = evalExprAST(ast);
            System.out.println(result);
        } else {
            System.out.println(parseResult);
        }
    }
    
}
