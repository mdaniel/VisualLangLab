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

/*
 The design of this class is inspired by the element of the same 
 name in the Scala (http://scala-lang.org/) standard library. 
 The Java code is also based on the corresponding Scala source.
 */

package net.java.vll.vllj.combinator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexParsers extends Parsers {
    
    protected Pattern whiteSpace = Pattern.compile("\\s+");
    
    public boolean skipWhitespace() {
        return whiteSpace.toString().length() > 0;
    }
    
    protected int handleWhiteSpace(CharSequence source, int offset) {
        if (skipWhitespace()) {
            Matcher m = whiteSpace.matcher(source.subSequence(offset, source.length()));
            if (m.lookingAt())
                return offset + m.end();
            else 
                return offset;
        } else {
            return offset;
        }
    }
    
    public Parser<String> literal(final String lit) {
        return literal(String.format("expected literal(%s)", lit), lit);
    }
    
    public Parser<String> literal(final String errMsg, final String lit) {
        return new Parser<String>() {
            @Override
            public ParseResult<String> apply(Reader input) {
                int offset2 = handleWhiteSpace(input.source(), input.offset());
                CharSequence cs = input.source();
                if (cs.subSequence(offset2, cs.length()).toString().startsWith(lit))
                    return new Success(lit, input.drop(offset2 - input.offset() + lit.length()));
                else
                    return new Failure(errMsg, input);
            }
        };
    }
    
    public Parser<String> regex(final Pattern p) {
        return regex(String.format("expected regex(%s)", p.toString()), p);
    }
    
    public Parser<String> regex(final String errMsg, final Pattern p) {
        return new Parser<String>() {
            @Override
            public ParseResult<String> apply(Reader input) {
                int offset2 = handleWhiteSpace(input.source(), input.offset());
//                Reader newInput = input.drop(offset2 - input.offset());
                CharSequence cs = input.source();
                Matcher m = p.matcher(cs.subSequence(offset2, cs.length()));
                if (m.lookingAt()) {
                    return new Success(m.group(), input.drop(offset2 - input.offset() + m.group().length()));
                } else {
                    return new Failure(errMsg, input);
                }
            }
        };
    }
    
    @Override
    public <T>Parser<T> phrase(final Parser<T> p) {
        return new Parser<T>() {
            @Override
            public ParseResult<T> apply(Reader input) {
                ParseResult<T> pr = p.apply(input);
//System.out.printf("%s: %s%n", p, pr);
                if (!pr.successful())
                    return new Failure("phrase", input, (Failure)pr);
                else if (pr.next().atEnd())
                    return pr;
                int step = handleWhiteSpace(pr.next().source(), pr.next().offset());
                if (pr.next().drop(step - pr.next().offset()).atEnd())
                    return pr;
                else
                    return new Failure(String.format("expected end of input at [%d, %d]", 
                            pr.next().line(), pr.next().column()), input);
            }
        };
    }
    
    public ParseResult<? extends Object> parseAll(Parser<? extends Object> p, CharSequence cs) {
        return phrase(p).apply(new CharSequenceReader(cs));
    }
    
    public ParseResult<? extends Object> parseAll(Parser<? extends Object> p, Reader rdr) {
        return phrase(p).apply(rdr);
    }
    
    public void reset() {
        tokenParserMap.clear();
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

    public Parser<String> literal2(String errMsg, String lit) {
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
    
    public Parser<String> regex2(String errMsg, Pattern pat) {
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
    
    public Parser<String> wildCard(String errMsg) {
        return parserById(errMsg, Integer.MAX_VALUE);
    }
    
    private Parser<String> parserById(final String errMsg, final int id) {
        return new Parser<String>() {
            @Override
            public ParseResult<String> apply(Reader input) {
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
                        if (regId == id || id == Integer.MAX_VALUE)
                            return new Success(reg, input.drop((Integer)regRes[2]));
                        else 
                            return new Failure(errMsg, input);
                    }
                } else {
                    if (reg == null) {
                        int litId = (Integer)litRes[1];
                        if (litId == id || id == Integer.MAX_VALUE)
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
    
    public String whiteSpaceRegex = Utils.reEscape(whiteSpace.pattern());
    public String commentSpecRegex = "";
    private Map<String, Parser<String>> tokenParserMap = new HashMap<String, Parser<String>>();
    private boolean setupDone = false;
    private List<String> theLiterals = new ArrayList<String>();
    private Object[][] sortedLiterals = null;
    private Matcher literalsMatcher = null;
    private List<String> theRegexs = new ArrayList<String>();
    private Matcher regexMatchers[] = null;

}
