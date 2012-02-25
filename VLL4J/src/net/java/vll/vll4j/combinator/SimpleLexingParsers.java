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

package net.java.vll.vll4j.combinator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLexingParsers extends RegexParsers {
    
    private static abstract class Lexer {
        abstract Object[] apply(Reader r);
    }
    
    public void reset() {
        tokenLexerMap.clear();
        setupDone = false;
        theLiterals.clear();
        sortedLiterals = null;
        literalsMatcher = null;
        theRegexs.clear();
        regexMatchers = null;
    }
    
    public void resetWhitespace() {
        String ws = Utils.unEscape(whiteSpaceRegex);
        String cmts = Utils.unEscape(commentSpecRegex);
        if (ws.isEmpty() && cmts.isEmpty()) {
            whiteSpace = Pattern.compile("");
        } else if (cmts.isEmpty()) {
            whiteSpace = Pattern.compile(ws);
        } else if (ws.isEmpty()) {
            whiteSpace = Pattern.compile(cmts);
        } else {
            String wsp = String.format("(?:(?:%s)|(?:%s))+", ws, cmts);
            whiteSpace = Pattern.compile(wsp);
        }
    }
    
    public void defineLiteral(String lit) {
        if (setupDone)
            throw new IllegalStateException();
        String litKey = "L" + Utils.escapeMetachars(lit);
        if (tokenLexerMap.containsKey(litKey)) {
            throw new IllegalArgumentException("Literal already defined");
        } else {
            theLiterals.add(lit);
            int id = -theLiterals.size(); // -1, -2, -3, etc.
            tokenLexerMap.put(litKey, lexerById(id));
        }
    }
    
    public void defineRegex(Pattern pat) {
        if (setupDone)
            throw new IllegalStateException();
        String regString = pat.toString();
        String regKey = "R" + regString;
        if (tokenLexerMap.containsKey(regKey)) {
            throw new IllegalStateException();
        } else {
            theRegexs.add(regString);
            int id = theRegexs.size() - 1; // 0, 1, 2, etc.
            tokenLexerMap.put(regKey, lexerById(id));
        }
    }

    public Parser<String> literal2(String errMsg, String lit) {
        String litKey = "L" + Utils.escapeMetachars(lit);
        Lexer lexer = null;
        if (tokenLexerMap.containsKey(litKey)) {
            return lexer2parser(tokenLexerMap.get(litKey), errMsg);
        } else {
            throw new IllegalArgumentException("Undefined literal");
        }
    }
    
    public Parser<String> regex2(String errMsg, Pattern pat) {
//System.out.printf("regex(%s, %s)%n", errMsg, pat.toString());
        String regString = pat.toString();
        String regKey = "R" + regString;
        if (tokenLexerMap.containsKey(regKey)) {
            return lexer2parser(tokenLexerMap.get(regKey), errMsg);
        } else {
            throw new IllegalArgumentException("Undefined regex");
        }
    }
    
    public Parser<String> wildCard(final String errMsg) {
        return lexer2parser(lexerById(Integer.MAX_VALUE), errMsg);
    }
    
    private Parser<String> lexer2parser(final Lexer lexer, final String errMsg) {
        return new Parser<String>() {
            public ParseResult apply(Reader in) {
                try {
                Object lexRes[] = lexer.apply(in);
                if (lexRes == null)
                    return new Failure(errMsg, in);
                else 
                    return new Success(lexRes[0], in.drop((Integer)lexRes[2]));
                } catch (StackOverflowError soe) {
                    throw new RuntimeException(String.format("Java bug 5050507 at (%d, %d)", in.line(), in.column()), soe);
                }
            }
        };
    }
    
    private Lexer lexerById(final int id) {
        return new Lexer() {
            Object[] apply(Reader input) {
                if (!setupDone) {
                    setupLexerLiterals();
                    setupLexerRegexs();
                    setupDone = true;
                }
                Object litRes[] = theLiterals.isEmpty() ? null : lexKnownLiterals(input);
                Object regRes[] = regexMatchers.length == 0 ? null : lexKnownRegexs(input);
                String lit = litRes == null ? null : (String)litRes[0];
                String reg = regRes == null ? null : (String)regRes[0];
//System.out.printf("parserById(%d): Lit=%s, Reg=%s%n", id, lit, reg);
                if (lit == null) {
                    if (reg == null) {
                        return null; //new Failure(errMsg, input);
                    } else {
                        int regId = (Integer)regRes[1];
                        if (regId == id || id == Integer.MAX_VALUE) {
                            return regRes; 
                        } else 
                            return null; 
                    }
                } else {
                    if (reg == null) {
                        int litId = (Integer)litRes[1];
                        if (litId == id || id == Integer.MAX_VALUE)
                            return litRes; 
                        else
                            return null; 
                    } else {
                        if (lit.length() >= reg.length()) {
                            int litId = (Integer)litRes[1];
                            if (litId == id)
                                return litRes; 
                            else
                                return null; 
                        } else {
                            int regId = (Integer)regRes[1];
                            if (regId == id)
                                return regRes; 
                            else 
                                return null; 
                        }
                    }
                }
            }
        };
    }
    
    private Object[] lexKnownLiterals(Reader input) {
        int offset = input.offset();
        int offset2 = handleWhiteSpace(input.source(), offset);
//System.out.printf("lexKnownLiterals(): offset=%d, offset2=%d%n", input.offset(), offset2);
        CharSequence cs = input.source().subSequence(offset2, input.source().length());
        literalsMatcher.reset(cs);
        if (literalsMatcher.lookingAt()) {
            int k = 1;
            String lexeme = null;
            while ((lexeme = literalsMatcher.group(k)) == null) {
                ++k;
            }
//System.out.printf("lexKnownLiterals(): k=%d, lexeme=%s%n", k, lexeme);
            int idx = (Integer)sortedLiterals[k - 1][1];
//System.out.printf("lexKnownLiterals(): idx=%d%n", idx);
            return new Object[] {lexeme, idx, offset2 - offset + lexeme.length()};
        } else {
//System.out.printf("lexKnownLiterals(): Failed: lookingAt(%s)%n", cs);
            return null;
        }
    }
    
    private Object[] lexKnownRegexs(Reader input) {
        int offset = input.offset();
        int offset2 = handleWhiteSpace(input.source(), offset);
        CharSequence cs = input.source().subSequence(offset2, input.source().length());
//System.out.printf("lexKnownRegexs(): CharSequence=%s%n", cs);
        int idx = -1;
        String lexeme = "", newLexeme = null;
        for (int i = 0; i < regexMatchers.length; ++i) {
            Matcher m = regexMatchers[i].reset(cs);
            if (m.lookingAt() && (newLexeme = m.group()).length() > lexeme.length()) {
                lexeme = newLexeme;
                idx = i;
            } 
        }
//System.out.printf("lexKnownRegexs(): idx=%d, lexeme=%s%n", idx, lexeme);
        if (idx == -1)
            return null;
        else
            return new Object[] {lexeme, idx, offset2 - offset + lexeme.length()};
    }
    
    private void setupLexerLiterals() {
        sortedLiterals = new Object[theLiterals.size()][];
        for (int i = 0; i < sortedLiterals.length; ++i) {
            sortedLiterals[i] = new Object[2];
            sortedLiterals[i][0] = theLiterals.get(i);
            sortedLiterals[i][1] = (i + 1) * -1;
//System.out.printf("setupLexerLiterals(): lit=%s id=%d%n", sortedLiterals[i][0], sortedLiterals[i][1]);
        }
        Comparator c = new Comparator() {
            @Override
            public int compare(Object a, Object b) {
                String aa = ((String)(((Object[])a)[0]));
                String bb = ((String)(((Object[])b)[0]));
                return ((String)bb).length() - ((String)aa).length();
            }
        };
        Arrays.sort(sortedLiterals, c);
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < sortedLiterals.length; ++i) {
            sb.append(Utils.escapeMetachars(Utils.unEscape((String)sortedLiterals[i][0])));
            if (i != sortedLiterals.length - 1)
                sb.append(")|(");
        }
        sb.append(")");
        String pattern = sb.toString();
        literalsMatcher = Pattern.compile(pattern).matcher("");
//System.out.println(allLiteralsPattern);
    }
    
    private void setupLexerRegexs() {
        regexMatchers = new Matcher[theRegexs.size()];
        for (int i = 0; i < regexMatchers.length; ++i) {
            regexMatchers[i] = Pattern.compile(theRegexs.get(i)).matcher("");
//System.out.println("Regex: " + theRegexs.get(i));
        }
    }
    
    public String whiteSpaceRegex = Utils.reEscape(whiteSpace.pattern());
    public String commentSpecRegex = "";
    private Map<String, Lexer> tokenLexerMap = new HashMap<String, Lexer>();
    private boolean setupDone = false;
    private List<String> theLiterals = new ArrayList<String>();
    private Object[][] sortedLiterals = null;
    private Matcher literalsMatcher = null;
    private List<String> theRegexs = new ArrayList<String>();
    private Matcher regexMatchers[] = null;

}
