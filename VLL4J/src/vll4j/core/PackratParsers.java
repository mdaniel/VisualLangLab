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

public class PackratParsers extends LexingRegexParsers {

    public static class PackratReader implements Reader {

        PackratReader(Reader underlying) {
            this.underlying = underlying;
            lrStack = new ArrayList<LR>();
            recursionHeads = new HashMap<Integer, Head>();
            cache = new HashMap<Object[], MemoEntry>();
        }

        PackratReader() {
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
        
        MemoEntry getFromCache(Parser p) {
            Object key = new Object[] {p, offset()};
            if (cache.containsKey(key))
                return cache.get(key);
            else
                return null;
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
    
    public <T> Parser<T> phrase(Parser<T> p) {
        final Parser<T> q = super.phrase(p);
        return new PackratParser<T>() {
            public ParseResult<T> parse(Reader r) {
                return q.parse(r instanceof PackratReader ? r : new PackratReader(r));
            }
        };
    }
    
    int getPosFromResult(ParseResult pr) {
        return pr.next().offset();
    }

    private static class MemoEntry<T> {
        MemoEntry(ParseResult r) {
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
        LR(ParseResult seed, Parser rule, Head head) {
            this.seed = seed;
            this.rule = rule;
            this.head = head;
        }
        int getPos() {
            return seed.next().offset();
        }
        ParseResult seed;
        Parser rule;
        Head head; // may be null
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
    
    <T>PackratParser<T> parser2packrat(Parser<T> p) {
        return null;
    }
    
    private MemoEntry recall(Parser p, PackratReader in) {
        MemoEntry cached = in.getFromCache(p);
        Head head = in.recursionHeads.containsKey(in.offset()) ? 
                in.recursionHeads.get(in.offset()) : null;
        if (head == null) {
            return cached;
        } else {
            Parser hp = head.headParser;
            List<Parser> involved = head.involvedSet;
            List<Parser> evalSet = head.involvedSet;
            if (cached == null && !(hp == p || involved.contains(p))) {
                return new MemoEntry(new Failure("dummy ", in, null));
            }
            if (evalSet.contains(p)) {
                for (int i = head.evalSet.size() - 1; i >= 0; --i) {
                    Parser pi = head.evalSet.get(i);
                    if (pi.equals(p))
                        head.evalSet.remove(pi);
                }
                ParseResult tempRes = p.parse(in);
                MemoEntry tempEntry = cached;
                tempEntry.r = tempRes;
            }
            return cached;
        }
    }
    
    void setupLR(Parser p, PackratReader in, LR recDetect) {
        if (recDetect.head == null)
            recDetect.head = new Head(p, new ArrayList<Parser>(), new ArrayList<Parser>());
        for (LR lr: in.lrStack) {
            if (lr.rule.equals(p))
                break;
            lr.head = recDetect.head;
            if (recDetect.head != null)
                recDetect.head.involvedSet.add(0, lr.rule);
        }
    }
    
    <T> ParseResult<T> lrAnswer(Parser<T> p, PackratReader in, LR growable) {
        ParseResult seed = growable.seed;
        Parser rule = growable.rule;
        Head head = growable.head;
        if (!head.getHead().equals(p)) {
            return seed;
        } else {
            in.updateCacheAndGet(p, new MemoEntry(seed));
            if (seed.successful())
                return grow(p, in, head);
            else
                return seed;
        }
    }
    
    <T> PackratParser<T> memo(final Parser<T> p) {
        return new PackratParser<T>() {
            @Override
            public ParseResult<T> parse(Reader in) {
                PackratReader inMem = in instanceof PackratReader ? 
                        (PackratReader)in : new PackratReader(in);
                MemoEntry m = recall(p, inMem);
                if (m.r == null) {
                    LR base = new LR(new Failure("Base failure", in, null), p, null);
                    inMem.lrStack.add(0, base);
                    inMem.updateCacheAndGet(p, new MemoEntry(base));
                    ParseResult tempRes = p.parse(in);
                    inMem.lrStack.remove(0);
                    if (base.head == null) {
                        inMem.updateCacheAndGet(p, new MemoEntry(tempRes));
                        return tempRes;
                    } else {
                        base.seed = tempRes;
                        return lrAnswer(p, inMem, base);
                    }
                } else {
                    Object mEntry = m.r;
                    if (mEntry instanceof LR) {
                        LR recDetect = (LR)mEntry;
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
        rest.recursionHeads.put(rest.offset(), head);
        ParseResult oldRes = (ParseResult)rest.getFromCache(p).r;
        head.evalSet = head.involvedSet;
        ParseResult tempRes = p.parse(rest);
        if (tempRes.successful()) {
            if (oldRes.next().offset() < tempRes.next().offset()) {
                rest.updateCacheAndGet(p, new MemoEntry(tempRes));
                return grow(p, rest, head);
            } else {
                rest.recursionHeads.remove(rest.offset());
                MemoEntry m = rest.getFromCache(p);
                //if (m instanceof ParseResult)
                    return (ParseResult)m;
            }
        } else {
            rest.recursionHeads.remove(rest.offset());
            return oldRes;
        }
    }
}
