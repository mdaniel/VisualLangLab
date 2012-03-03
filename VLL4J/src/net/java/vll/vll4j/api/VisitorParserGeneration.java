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

package net.java.vll.vll4j.api;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.script.ScriptException;
import net.java.vll.vll4j.combinator.Parsers.Failure;
import net.java.vll.vll4j.combinator.Parsers.ParseResult;
import net.java.vll.vll4j.combinator.Parsers.Parser;
import net.java.vll.vll4j.combinator.Parsers.Success;
import net.java.vll.vll4j.combinator.Reader;
import net.java.vll.vll4j.combinator.Utils;

public class VisitorParserGeneration extends VisitorBase {
    
    public VisitorParserGeneration(Forest theForest, ApiParsers parsersInstance, boolean traceAll) {
        parsersInstance.reset();
        this.theForest = theForest;
        this.parsersInstance = parsersInstance;
        this.traceAll = traceAll;
        createTokenParsers();
        visitorNodeValidation = new VisitorValidation();
        parserGeneratedOk = true;
    }
    
/*    void reset() {
        gui.regexParsers.reset();
        parserCache.clear();
        parserGeneratedOk = true;
        traceLevel = 0;
    }*/
    
    private void createTokenParsers() {
        for (Map.Entry<String, String> e: theForest.tokenBank.entrySet()) {
            if (e.getKey().endsWith("_"))
                continue;
            String pat = e.getValue();
            if (pat.startsWith("L")) {
                parsersInstance.defineLiteral(Utils.unEscape(pat.substring(1)));
            } else if (pat.startsWith("R")) {
                parsersInstance.defineRegex(Pattern.compile(Utils.unEscape(pat.substring(1))));
            } else 
                throw new IllegalArgumentException("Bad token");
        }
    }
    
    private void traceIndent() {
        for (int i = 0; i < traceLevel; ++i) {
            System.out.print("|  ");
        }
    }
    
    Parser<? extends Object> withAction(final Parser<? extends Object> p, final NodeBase node) {
        if (!(node instanceof NodeSemPred) && node.actionFunction != null) {
            Parser<? extends Object> pm = new Parser() {
                @Override
                public ParseResult<? extends Object> apply(Reader input) {
                    try {
                        node.actionFunction.run(null, input, input.source().length());
                        ParseResult<? extends Object> res = p.apply(input);
                        if (res.successful()) {
                            int postWhitespace = parsersInstance.handleWhiteSpace(input.source(), input.offset());
                            Reader forAction = input.drop(postWhitespace - input.offset());
                            Object r2 = node.actionFunction.run(res.get(), forAction, res.next().offset());
                            return new Success(r2, res.next());
                        } else 
                            return res;
                    } catch (ScriptException exc) {
                        throw new IllegalArgumentException(String.format("Error in action-code @ %s", node.nodeName()), exc);
                    } catch (IllegalArgumentException exc) {
                        throw exc;
                    }
                }
            };
            return pm;
        } else {
            return p;
        }
    }
    
    Parser<? extends Object> withTrace(final Parser<? extends Object> p, final NodeBase node) {
        if (node.isTraced || ((node instanceof NodeRoot) && traceAll)) {
            Parser<? extends Object> pm = new Parser() {
                @Override
                public ParseResult<? extends Object> apply(Reader input) {
                    traceIndent();
                    System.out.print(String.format(">> %s (line=%d, col=%d)%n", node.nodeName(), input.line(), input.column()));
                    ++traceLevel;
                    int postWhitespace = parsersInstance.handleWhiteSpace(input.source(), input.offset());
                    String sample = Utils.reEscape(input.source().subSequence(postWhitespace, 
                            Math.min(input.source().length(), postWhitespace + 20)).toString());
                    ParseResult<? extends Object> res = p.apply(input);
                    --traceLevel;
                    traceIndent();
                    System.out.print(String.format("<< %s: %s (line=%d, col=%d, input=%s)%n", node.nodeName(), res.getClass().getSimpleName(),
                            res.next().line(), res.next().column(), sample));
                    return res;
                }
            };
            return withAction(pm, node);
        } else {
            return withAction(p, node);
        }
    }
    
    Parser<? extends Object> withMultiplicity(Parser<? extends Object> p, NodeBase node) {
        Parser<? extends Object> pm = p;
        if (node.multiplicity == Multiplicity.ZeroOrMore) {
            pm = parsersInstance.rep(p);
        } else if (node.multiplicity == Multiplicity.OneOrMore) {
            pm = parsersInstance.rep1(String.format("rep1(%s)", node.nodeName()), p);
        } else if (node.multiplicity == Multiplicity.ZeroOrOne) {
            pm = parsersInstance.opt(p);
        } else if (node.multiplicity == Multiplicity.Not) {
            pm = parsersInstance.not(p);
        } else if (node.multiplicity == Multiplicity.Guard) {
            pm = parsersInstance.guard(p);
        }
        return withTrace(pm, node);
    }
    
    @Override
    public Parser<? extends Object> visitChoice(NodeChoice n) {
        int childCount = n.getChildCount();
        if (n.accept(visitorNodeValidation) == null) {
            Parser<? extends Object> cp[] = (Parser<? extends Object>[]) new Parser[childCount];
            for (int i = 0; i < childCount; ++i) {
                cp[i] = (Parser<? extends Object>)((NodeBase) n.getChildAt(i)).accept(this);
            }
            return withMultiplicity(parsersInstance.choice(n.errorMessage.isEmpty() ? 
                    String.format("choice(%s)", n.nodeName()) : n.errorMessage, cp), n);
        } else {
            parserGeneratedOk = false;
            return null;
        }
    }
    
    @Override
    public Parser<? extends Object> visitLiteral(NodeLiteral n) {
        if (n.accept(visitorNodeValidation) == null) {
            String litString = Utils.unEscape(theForest.tokenBank.get(n.literalName).substring(1));
            String errMsg = n.errorMessage.isEmpty() ? 
                    String.format("literal:%s(%s)", n.literalName, n.nodeName()) : n.errorMessage;
            return withMultiplicity(n.literalName.endsWith("_") ? parsersInstance.literal(errMsg, litString) : parsersInstance.literal2(errMsg, litString), n);
        } else {
            parserGeneratedOk = false;
            return null;
        }
    }

    @Override
    public Parser<? extends Object> visitReference(NodeReference n) {
        if (n.accept(visitorNodeValidation) == null) {
            final String referredRuleName = n.refRuleName;
            NodeBase referredRule = theForest.ruleBank.get(referredRuleName);
            if (!parserCache.containsKey(referredRuleName)) {
                referredRule.accept(this);
            }
            final Parser<? extends Object> holder[] = parserCache.get(referredRuleName);
            Parser<? extends Object> p = new Parser() {
                @Override
                public ParseResult<? extends Object> apply(Reader input) {
                    try {
                        return holder[0].apply(input);
                    } catch (StackOverflowError soe) {
                        throw new RuntimeException(String.format("Possible left-recursion in '%s'", referredRuleName), soe);
                    }
                }
            };
            return withMultiplicity(p, n);
        } else {
            parserGeneratedOk = false;
            return null;
        }
    }

    @Override
    public Parser<? extends Object> visitRegex(NodeRegex n) {
        if (n.accept(visitorNodeValidation) == null) {
            String regString = Utils.unEscape(theForest.tokenBank.get(n.regexName).substring(1));
            String errMsg = n.errorMessage.isEmpty() ? 
                    String.format("regex:%s(%s)", n.regexName, n.nodeName()) : n.errorMessage;
            return withMultiplicity(n.regexName.endsWith("_") ? 
                    parsersInstance.regex(errMsg, Pattern.compile(regString)) : 
                    parsersInstance.regex2(errMsg, Pattern.compile(regString)), n);
        } else {
            parserGeneratedOk = false;
            return null;
        }
    }
    
    @Override
    public Parser<? extends Object> visitRepSep(NodeRepSep n) {
        if (n.accept(visitorNodeValidation) == null) {
            Parser<Object> rep = (Parser<Object>)((NodeBase) n.getChildAt(0)).accept(this);
            Parser<Object> sep = (Parser<Object>)((NodeBase) n.getChildAt(1)).accept(this);
            if (n.multiplicity == Multiplicity.ZeroOrMore)
                return withTrace(parsersInstance.repSep(rep, sep), n);
            else if (n.multiplicity == Multiplicity.OneOrMore)
                return withTrace(parsersInstance.rep1Sep(n.errorMessage.isEmpty() ?
                        String.format("rep1sep(%s) error", n.nodeName()) : n.errorMessage, rep, sep), n);
            else {
                parserGeneratedOk = false;
                return null;
            }
        } else {
            parserGeneratedOk = false;
            return null;
        }
    }
    
    @Override
    public Parser<? extends Object> visitRoot(NodeRoot n) {
        Parser<? extends Object> p;
        Parser holder[] = new Parser[1];
        parserCache.put(n.ruleName, holder);
        if (n.accept(visitorNodeValidation) == null) {
            p = (Parser<? extends Object>)((NodeBase) n.getChildAt(0)).accept(this);
        } else {
            parserGeneratedOk = false;
            p = null;
        }
        if (n.isPackrat) {
            p = parsersInstance.parser2packrat(p);
        }
        holder[0] = withTrace(p, n);
        return holder[0];
    }
    
    @Override
    public Parser<? extends Object> visitSemPred(final NodeSemPred n) {
        if (n.accept(visitorNodeValidation) == null) {
            Parser<? extends Object> parser = new Parser() {
                @Override
                public ParseResult<? extends Object> apply(Reader input) {
                    Object result = null;
                    try {
                        result = n.actionFunction.run(null, input, input.source().length());
                    } catch (ScriptException ex) {
                    }
                    if (result == Boolean.TRUE) {
                        return new Success(null, input);
                    } else {
                        return new Failure(n.errorMessage.isEmpty() ? 
                        String.format("SemPred(%s)", n.nodeName()) : n.errorMessage, input);
                    }
                }
            };
            return withTrace(parser, n);
        } else {
            parserGeneratedOk = false;
            return null;
        }
    }

    @Override
    public Parser<? extends Object> visitSequence(NodeSequence n) {
        int childCount = n.getChildCount();
        if (n.accept(visitorNodeValidation) == null) {
            int dropMap = 0;
            Parser<? extends Object> cp[] = (Parser<? extends Object>[]) new Parser[childCount];
            for (int i = 0, mask = 1; i < childCount; ++i, mask <<= 1) {
                NodeBase child = (NodeBase) n.getChildAt(i);
                cp[i] = (Parser<? extends Object>) child.accept(this);
                if (child.isDropped || child.multiplicity == Multiplicity.Guard || 
                        child.multiplicity == Multiplicity.Not || child instanceof NodeSemPred)
                    dropMap |= mask;
            }
            return withMultiplicity(parsersInstance.sequence(n.errorMessage.isEmpty() ?
                    String.format("sequence(%s)", n.nodeName()) : n.errorMessage, dropMap, cp), n);
        } else {
            parserGeneratedOk = false;
            return null;
        }
    }
    
    @Override
    public Parser<? extends Object> visitWildCard(NodeWildCard n) {
        if (n.accept(visitorNodeValidation) == null) {
            String errMsg = n.errorMessage.isEmpty() ? 
                    String.format("wildCard(%s)", n.nodeName()) : n.errorMessage;
            return withMultiplicity(parsersInstance.wildCard(errMsg), n);
        } else {
            parserGeneratedOk = false;
            return null;
        }
    }

    private ApiParsers parsersInstance;
    private boolean traceAll;
    private Map<String, Parser<? extends Object>[]> parserCache = new HashMap<String, Parser<? extends Object>[]>();
    public boolean parserGeneratedOk;
    private int traceLevel = 0;
    private VisitorValidation visitorNodeValidation;
    private Forest theForest;
}
