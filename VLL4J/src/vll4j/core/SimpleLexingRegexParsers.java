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

package vll4j.core;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleLexingRegexParsers extends RegexParsers {
    
    public void reset() {
        tokenParserMap.clear();
        setupDone = false;
        theLiterals.clear();
        sortedLiterals = null;
        literalsMatcher = null;
        theRegexs.clear();
        regexMatchers = null;
    }
    
    public void setWhitespace(Pattern p) {
        whiteSpace = p;
    }
    
    public String getWhitespace() {
        return whiteSpace.toString();
    }
    
    @Override
    public Parser<String> literal(String errMsg, String lit) {
        if (setupDone)
            throw new IllegalStateException();
        String litKey = "L" + Utils.escapeMetachars(lit);
        if (tokenParserMap.containsKey(litKey))
            return tokenParserMap.get(litKey);
        theLiterals.add(lit);
        int id = -theLiterals.size(); // -1, -2, -3, etc.
        Parser<String> p = parserById(errMsg, id);
        tokenParserMap.put(litKey, p);
        return p;
    }
    
    @Override
    public Parser<String> regex(String errMsg, Pattern pat) {
//System.out.printf("regex(%s, %s)%n", errMsg, pat.toString());
        if (setupDone)
            throw new IllegalStateException();
        String regString = pat.toString();
        String regKey = "R" + regString;
        if (tokenParserMap.containsKey(regKey))
            return tokenParserMap.get(regKey);
        theRegexs.add(regString);
        int id = theRegexs.size() - 1; // 0, 1, 2, etc.
        Parser<String> p = parserById(errMsg, id);
        tokenParserMap.put(regKey, p);
        return p;
    }
    
    private Parser<String> parserById(final String errMsg, final int id) {
        return new Parser<String>() {
            @Override
            public ParseResult<String> parse(Reader input) {
                if (!setupDone) {
                    setupLexer();
                    setupDone = true;
                }
                Object litRes[] = theLiterals.isEmpty() ? null : lexKnownLiterals(input);
                Object regRes[] = regexMatchers.length == 0 ? null : lexKnownRegexs(input);
                String lit = litRes == null ? null : (String)litRes[0];
                String reg = regRes == null ? null : (String)regRes[0];
//System.out.printf("parserById(%d): Lit=%s, Reg=%s%n", id, lit, reg);
                if (lit == null) {
                    if (reg == null) {
                        return new Failure(errMsg, input);
                    } else {
                        int regId = (Integer)regRes[1];
                        if (regId == id)
                            return new Success(reg, input.drop((Integer)regRes[2]));
                        else 
                            return new Failure(errMsg, input);
                    }
                } else {
                    if (reg == null) {
                        int litId = (Integer)litRes[1];
                        if (litId == id)
                            return new Success(lit, input.drop((Integer)litRes[2]));
                        else
                            return new Failure(errMsg, input);
                    } else {
                        if (lit.length() >= reg.length()) {
                            int litId = (Integer)litRes[1];
                            if (litId == id)
                                return new Success(lit, input.drop((Integer)litRes[2]));
                            else
                                return new Failure(errMsg, input);
                        } else {
                            int regId = (Integer)regRes[1];
                            if (regId == id)
                                return new Success(reg, input.drop((Integer)regRes[2]));
                            else 
                                return new Failure(errMsg, input);
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
    
    private void setupLexer() {
        setupLexerLiterals();
        setupLexerRegexs();
    }
    
    public Parser<String> literal$(String errMsg, String lit) {
        return super.literal(errMsg, lit);
    }
    
    public Parser<String> literal$(String lit) {
        return super.literal(lit);
    }
    
    public Parser<String> regex$(String errMsg, String p) {
        return super.regex(errMsg, p);
    }
    
    public Parser<String> regex$(String p) {
        return super.regex(p);
    }
    
    private Map<String, Parser<String>> tokenParserMap = new HashMap<String, Parser<String>>();
    private boolean setupDone = false;
    private List<String> theLiterals = new ArrayList<String>();
    private Object[][] sortedLiterals = null;
    private Matcher literalsMatcher = null;
    private List<String> theRegexs = new ArrayList<String>();
    private Matcher regexMatchers[] = null;

    private static void testOnlyLiterals() {
        System.out.println("Testing Literals");
        SimpleLexingRegexParsers me = new SimpleLexingRegexParsers();
        String pats[] = new String[] {"hello", "hello\tWorld", "i*s", "into"};
        Parser<String> hello = me.literal(pats[0], pats[0]);
        Parser<String> helloWorld = me.literal(pats[1], pats[1]);
        Parser<String> iss = me.literal(pats[2], pats[2]);
        Parser<String> into = me.literal(pats[3], pats[3]);
        Parser seq = me.sequence("sequence", 0, hello, helloWorld, iss, into);
        ParseResult res = me.parseAll(seq, "  hello  hello\tWorld i*s into");
        System.out.println(me.dumpResult(res));
        System.out.println(me.dumpValue(res.get()));
    }

    private static void testOnlyRegexs() {
        System.out.println("Testing Regexs");
        SimpleLexingRegexParsers me = new SimpleLexingRegexParsers();
        String regPats[] = new String[] {"[0-9]+", "[a-z]+"};
        Parser<String> numPat = me.regex(regPats[0], regPats[0]);
        Parser<String> wordPat = me.regex(regPats[1], regPats[1]);
        Parser nums = me.rep1(me.choice("choice", numPat, wordPat));
        ParseResult res2 = me.parseAll(nums, "123  hello 2011 this is 1984");
        System.out.println(me.dumpResult(res2));
        System.out.println(me.dumpValue(res2.get())); 
    }

    private static void testBoth() {
        System.out.println("Testing Both");
        SimpleLexingRegexParsers me = new SimpleLexingRegexParsers();
        
        String regPats[] = new String[] {"[0-9]+", "[a-z]+"};
        Parser<String> numPat = me.regex(regPats[0], regPats[0]);
        Parser<String> wordPat = me.regex(regPats[1], regPats[1]);
        
        String pats[] = new String[] {"hello", "this"};
        Parser<String> hello = me.literal(pats[0], pats[0]);
        Parser<String> thiss = me.literal(pats[1], pats[1]);

        Parser numOrWord = me.choice("choice", numPat, wordPat);
        Parser theSeq = me.sequence("sequence", 0, hello, numOrWord, thiss, numOrWord, numOrWord);
        ParseResult res2 = me.parseAll(theSeq, "hello 2011 this is 1984");
        System.out.println(me.dumpResult(res2));
        System.out.println(me.dumpValue(res2.get())); 
    }

    public static void main(String args[]) {
        testOnlyLiterals();
//        testOnlyRegexs();
//        testBoth();
    }
}
