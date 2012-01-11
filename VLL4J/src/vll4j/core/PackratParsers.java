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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PackratParsers extends LexingRegexParsers {

    public static class PackratReader implements Reader {
        PackratReader(Reader underlying) {
            this.underlying = underlying;
            lrStack = new ArrayList<LR>();
            recursionHeads = new HashMap<Integer, Head>();
            cache = new HashMap<Object[], MemoEntry>();
        }
        private PackratReader() {
        }
        @Override
        public boolean atEnd() {
            return underlying.atEnd();
        }
        @Override
        public int column() {
            return underlying.column();
        }
        @Override
        public Reader drop(int n) {
            PackratReader pr = new PackratReader();
            pr.cache = outer.cache;
            pr.lrStack = outer.lrStack;
            pr.recursionHeads = outer.recursionHeads;
            pr.underlying = outer.underlying.drop(n);
            return pr;
        }
        @Override
        public char first() {
            return underlying.first();
        }
        Option<MemoEntry> getFromCache(Parser p) {
            Object key = new Object[] {p, offset()};
            if (cache.containsKey(key))
                return new Some<MemoEntry>(cache.get(key));
            else
                return new None<MemoEntry>();
        }
        @Override
        public int line() {
            return underlying.line();
        }
        @Override
        public int offset() {
            return underlying.offset();
        }
        @Override
        public Reader rest() {
            return drop(1);
        }
        @Override
        public CharSequence source() {
            return underlying.source();
        }
        MemoEntry updateCacheAndGet(Parser p, MemoEntry w) {
            cache.put(new Object[] {p, offset()}, w);
            return w;
        }
        private Reader underlying;
        List<LR> lrStack = null;
        Map<Integer, Head> recursionHeads = null;
        Map<Object[], MemoEntry> cache = null;
        private PackratReader outer = this;
    }

    @Override
    public <T> Parser<T> phrase(Parser<T> p) {
System.out.println("phrase()");
        final Parser<T> q = super.phrase(p);
        return new PackratParser<T>() {
            public ParseResult<T> parse(Reader r) {
System.out.println("phrase-parse()");
                return memo(q).parse(r instanceof PackratReader ? r : new PackratReader(r));
            }
        };
    }

    private static class MemoEntry<T> {
        MemoEntry(ParseResult<T> r) {
            this.r = r;
        }
        MemoEntry(LR r) {
            this.r = r;
        }
        ParseResult<T> getResult() {
            return (r instanceof LR) ? ((LR)r).seed : (ParseResult)r;
        }
        Object r; // contains either LR or ParseResult
    }

    static class LR {
        LR(ParseResult seed, Parser rule, Option<Head> head) {
            this.seed = seed;
            this.rule = rule;
            this.head = head; 
        }
        int getPos() {
            return seed.next().offset();
        }
        ParseResult seed;
        Parser rule;
        Option<Head> head; 
    }

    static class Head {
        Head(Parser headParser, List<Parser> involvedSet, List<Parser> evalSet) {
            this.headParser = headParser;
            this.involvedSet = involvedSet;
            this.evalSet = evalSet;
        }
        Parser getHead() {
            return headParser;
        }
        Parser headParser;
        List<Parser> involvedSet = new ArrayList<Parser>();
        List<Parser> evalSet = new ArrayList<Parser>();
    }

    static abstract class PackratParser<T> extends Parser<T> {};
    
    public <T>PackratParser<T> parser2packrat(Parser<T> p) {
System.out.println("parser2packrat");
        return memo(p);
    }

    private Option<MemoEntry> recall(Parser p, PackratReader in) {
        Option<MemoEntry> cached = in.getFromCache(p);
        Option<Head> head = in.recursionHeads.containsKey(in.offset()) ? 
                new Some<Head>(in.recursionHeads.get(in.offset())) : new None<Head>();
System.out.printf("recall cached:%s, head:%s%n", cached, head);
        if (head.isEmpty()) {
            return cached;
        } else {
            Parser hp = head.get().headParser;
            List<Parser> involved = head.get().involvedSet;
            List<Parser> evalSet = head.get().evalSet;
            if (cached.isEmpty() && !(hp.equals(p) || involved.contains(p))) {
                return new Some<MemoEntry>(new MemoEntry(new Failure("dummy ", in, null)));
            }
            if (evalSet.contains(p)) {
                for (int i = evalSet.size() - 1; i >= 0; --i) {
                    Parser pi = evalSet.get(i);
                    if (pi.equals(p))
                        evalSet.remove(pi);
                }
                ParseResult tempRes = p.parse(in);
                Option<MemoEntry> tempEntry = cached;
                tempEntry.get().r = tempRes;
            }
            return cached;
        }
    }

    void setupLR(Parser p, PackratReader in, LR recDetect) {
System.out.println("setupLR");
        if (recDetect.head.isEmpty())
            recDetect.head = new Some<Head>(new Head(p, new ArrayList<Parser>(), new ArrayList<Parser>()));
        for (LR lr: in.lrStack) {
            if (lr.rule.equals(p))
                break;
            lr.head = recDetect.head;
            if (!recDetect.head.isEmpty())
                recDetect.head.get().involvedSet.add(0, lr.rule);
        }
    }

    <T> ParseResult<T> lrAnswer(Parser<T> p, PackratReader in, LR growable) {
        ParseResult seed = growable.seed;
        Parser rule = growable.rule;
        Option<Head> head = growable.head;
System.out.printf("lrAnswer seed:%s rule:%s head:%s%n", seed, rule, head);
        if (head.isEmpty())
            throw new IllegalArgumentException("lrAnswer with no head !!");
        if (!head.get().getHead().equals(p)) {
            return seed;
        } else {
            in.updateCacheAndGet(p, new MemoEntry(seed));
            if (seed.successful())
                return grow(p, in, head.get());
            else
                return seed;
        }
    }

    <T> PackratParser<T> memo(final Parser<T> p) {
System.out.println("memo");
        return new PackratParser<T>() {
            @Override
            public ParseResult<T> parse(Reader in) {
                PackratReader inMem = in instanceof PackratReader ? 
                        (PackratReader)in : new PackratReader(in);
                Option<MemoEntry> m = recall(p, inMem);
                if (m.isEmpty()) {
System.out.println("memo-parse-empty");
                    LR base = new LR(new Failure("Base failure", in, null), p, new None<Head>());
                    inMem.lrStack.add(0, base);
                    inMem.updateCacheAndGet(p, new MemoEntry(base));
                    ParseResult tempRes = p.parse(in);
                    inMem.lrStack.remove(0);
                    if (base.head.isEmpty()) {
                        inMem.updateCacheAndGet(p, new MemoEntry(tempRes));
                        return tempRes;
                    } else {
                        base.seed = tempRes;
                        return lrAnswer(p, inMem, base);
                    }
                } else {
System.out.println("memo-parse-empty-else");
                    Object mEntry = m.get().r;
                    if (mEntry instanceof LR) {
                        LR recDetect = (LR)mEntry;
                        setupLR(p, inMem, recDetect);
                        return recDetect.seed;
                    } else {
                        ParseResult res = (ParseResult)mEntry;
                        return res;
                    }
                }
            }
        };
    }

    ParseResult grow(Parser p, PackratReader rest, Head head) {
System.out.println("grow");
        rest.recursionHeads.put(rest.offset(), head);
        ParseResult oldRes = (ParseResult)rest.getFromCache(p).get().r;
        head.evalSet = head.involvedSet;
        ParseResult tempRes = p.parse(rest);
        if (tempRes.successful()) {
            if (oldRes.next().offset() < tempRes.next().offset()) {
                rest.updateCacheAndGet(p, new MemoEntry(tempRes));
                return grow(p, rest, head);
            } else {
                rest.recursionHeads.remove(rest.offset());
                MemoEntry m = rest.getFromCache(p).get();
                //if (m instanceof ParseResult)
                    return (ParseResult)m;
            }
        } else {
            rest.recursionHeads.remove(rest.offset());
            return oldRes;
        }
    }
    
    public static void main(String args[]) {
        PackratParsers pp = new PackratParsers();
        Parser nbr = pp.regex2("nbr", Pattern.compile("[0-9]+"));
        Parser plus = pp.literal2("PLUS", "+");
        Parser minus = pp.literal2("MINUS", "-");
        Parser plusOrMinus = pp.choice("plusOrMinus", plus, minus);
        Parser suffix = pp.rep(pp.sequence(0, plusOrMinus, nbr));
        Parser expr = pp.sequence(0, nbr, suffix);
        ParseResult pr = pp.parseAll(expr, "123 + 5 - 7 + 23 - 567");
        if (pr.successful())
            System.out.println(pp.dumpValue(pr.get()));
        else 
            System.out.println(pp.dumpResult(pr));
    }
}
