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

package vll4j.core;

import java.util.ArrayList;
import java.util.List;

public class Parsers {
    
    public static abstract class Parser<T> {
        public abstract ParseResult<T> apply(Reader input);
        public String name;
    }
    
    public static interface ParseResult<T> {
        public T get();
        public boolean successful();
        public Reader next();
    }

    public static class NoSuccess<T> implements ParseResult<T> {
        public NoSuccess(String msg, Reader next) {
            this(msg, next, null);
        }
        public NoSuccess(String msg, Reader next, NoSuccess reason) {
            this.msg = msg;
            this.next = next;
            this.reason = reason; // ++SPC (not in Scala parser combinators)
            lastNoSuccess = this;
        }
        @Override
        public T get() {return null;}
        @Override
        public boolean successful() {return false;}
        @Override
        public Reader next() {return next;}
        public final String msg;
        public NoSuccess reason = null;
        private Reader next;
    }

    public static class Success<T> implements ParseResult<T> {
        public Success(T result, Reader next) {
            this.result = result;
            this.next = next;
        }
        @Override
        public T get() {return result;}
        @Override
        public boolean successful() {return true;}
        @Override
        public Reader next() {return next;}
        private final T result;
        private Reader next;
    }

    public static class Failure<T> extends NoSuccess<T> {
        public Failure(String msg, Reader next) {
            this(msg, next, null);
        }
        public Failure(String msg, Reader next, NoSuccess reason) {
            super(msg, next, reason);
        }
    }
    
    public static class Error extends NoSuccess {
        public Error(String msg, Reader next) {
            super(msg, next);
        }
    }
    
/*    public static class Choice { //<T> {
        public Choice(int discr, Object value) {
            this.discr = discr;
            this.value = value;
        }
        public int discr;
        public Object value;
    }

*/    public <T>Parser<T> phrase(final Parser<T> p) {
        lastNoSuccess = null;
        return new Parser<T>() {
            @Override
            public ParseResult<T> apply(Reader input) {
                ParseResult<T> pr = p.apply(input);
                if (pr.next().atEnd())
                    return pr;
                else 
                    return new Failure("expected end of input", input);
            }
        };
    }
    
    public Parser<Object> failure(final String msg) {
        return new Parser<Object>() {
            @Override
            public ParseResult<Object> apply(Reader input) {
                return new Failure(msg, input);
            }
        };
    }
    
    public <T>Parser<T> success(final T value) {
        return new Parser<T>() {
            @Override
            public ParseResult<T> apply(Reader input) {
                return new Success(value, input);
            }
        };
    }
    
    public <T>Parser<T> guard(final Parser<T>  p) {
        return new Parser<T>() {
            @Override
            public ParseResult<T> apply(Reader input) {
                ParseResult<T> pr = p.apply(input);
                if (pr.successful()) 
                    return new Success<T>(pr.get(), input);
                else
                    return new Failure("??guard??", input);
            }
        };
    }
    
    public <T>Parser<T> not(final Parser<T>  p) {
        return new Parser<T>() {
            @Override
            public ParseResult<T> apply(Reader input) {
                ParseResult<T> pr = p.apply(input);
                if (pr.successful()) 
                    return new Failure("??not??", input);
                else
                    return new Success<T>(pr.get(), input);
            }
        };
    }
    
    public <T>Parser<Object[]> opt(final Parser<T> p) {
        return new Parser<Object[]>() {
            @Override
            public ParseResult<Object[]> apply(Reader input) {
                ParseResult<T> pr = p.apply(input);
                if (pr.successful())
                    return new Success(new Object[]{pr.get()}, pr.next());
                else
                    return new Success(new Object[]{null}, input);
            }
        };
    }
    
    public <T>Parser<List<T>> rep(final Parser<T> p) {
        return new Parser<List<T>>() {
            @Override
            public ParseResult<List<T>> apply(Reader input) {
                List<T> list = new ArrayList<T>();
                ParseResult<T> pr = null;
                while ((pr = p.apply(input)).successful()) {
                    list.add(pr.get());
                    input = pr.next();
                }
                return new Success(list, input);
            }
        };
    }
    
    public <T>Parser<List<T>> repSep(final Parser<T> rep, final Parser<T> sep) {
        return new Parser<List<T>>() {
            @Override
            public ParseResult<List<T>> apply(Reader input) {
                List<T> list = new ArrayList<T>();
                ParseResult<T> pr = null;
                if ((pr = rep.apply(input)).successful()) {
                    list.add(pr.get());
                    input = pr.next();
                    while ((pr = sep.apply(input)).successful() && 
                            (pr = rep.apply(pr.next())).successful()) {
                        list.add(pr.get());
                        input = pr.next();
                    }
                }
                return new Success(list, input);
            }
        };
    }
    
    public <T>Parser<List<T>> rep1(final Parser<T> p) {
        return rep1("??rep1??", p);
    }
    
    public <T>Parser<List<T>> rep1(final String errMsg, final Parser<T> p) {
        return new Parser<List<T>>() {
            @Override
            public ParseResult<List<T>> apply(Reader input) {
                List<T> list = new ArrayList<T>();
                ParseResult<T> pr = null;
                while ((pr = p.apply(input)).successful()) {
                    list.add(pr.get());
                    input = pr.next();
                }
                if (list.isEmpty())
                    return new Failure(errMsg, input, (NoSuccess<T>)pr);
                else
                    return new Success(list, input);
            }
        };
    }
    
    public <T>Parser<List<T>> rep1Sep(final Parser<T> rep, final Parser<T> sep) {
        return rep1Sep("??rep1sep??", rep, sep);
    }
    
    public <T>Parser<List<T>> rep1Sep(final String errMsg, final Parser<T> rep, final Parser<T> sep) {
        return new Parser<List<T>>() {
            @Override
            public ParseResult<List<T>> apply(Reader input) {
                List<T> list = new ArrayList<T>();
                ParseResult<T> pr = null;
                if ((pr = rep.apply(input)).successful()) {
                    list.add(pr.get());
                    input = pr.next();
                    while ((pr = sep.apply(input)).successful() && 
                            (pr = rep.apply(pr.next())).successful()) {
                        list.add(pr.get());
                        input = pr.next();
                    }
                }
                if (list.isEmpty())
                    return new Failure(errMsg, input, (NoSuccess<T>)pr);
                else
                    return new Success(list, input);
            }
        };
    }
    
    public Parser<Object[]> sequence(int dropMap, Parser<? extends Object>  ...p) {
        return sequence("??sequence??", dropMap, p);
    }
    
    public Parser<Object[]> sequence(final String errMsg, final int dropMap, final Parser<? extends Object>  ...p) {
        return new Parser<Object[]>() {
            @Override
            public ParseResult<Object[]> apply(Reader input) {
                int bc = Integer.bitCount(dropMap);
                Object res[] = new Object[p.length - bc];
                Reader inputOriginal = input;
                ParseResult<? extends Object> pr = null;
                for (int i = 0, j = 0, mask = 1; i < p.length; ++i, mask <<= 1) {
                    pr = p[i].apply(input);
                    if (!pr.successful())
                        break;
                    if ((dropMap & mask) == 0) {
                        res[j++] = pr.get();
                    }
                    input = pr.next();
                }
                if (pr.successful()) {
                    if (res.length == 1)
                        return new Success(res[0], input);
                    else
                        return new Success(res, input);
                } else
                    return new Failure(errMsg, inputOriginal, (NoSuccess<? extends Object>)pr);
            }
        };
    }
    
    public Parser<Object[]/*<Object>*/> choice(final Parser<? extends Object>  ...p) {
        return choice("??choice??", p);
    }
    
    public Parser<Object[]/*<Object>*/> choice(final String errMsg, final Parser<? extends Object>  ...p) {
        return new Parser<Object[]/*<Object>*/>() {
            @Override
            public ParseResult<Object[]/*<Object>*/> apply(Reader input) {
                ParseResult<? extends Object> pr = null;
                int n = 0;
                for (n = 0; n < p.length; ++n) {
                    pr = p[n].apply(input);
                    if (pr.successful())
                        break;
                }
                if (pr.successful())
                    return new Success<Object[]/*<Object>*/>(new Object[] {n, pr.get()}, pr.next());
                else
                    return new Failure(errMsg, input, (NoSuccess<? extends Object>)pr);
            }
        };
    }
    
    public static NoSuccess lastNoSuccess = null;
    
    public String dumpResult(ParseResult pr) {
        if (pr instanceof Success) {
            Success s = (Success)pr;
            return String.format("Success(%d, %d): %s", s.next().line(), s.next().column(), s.get());
        } else if (pr instanceof NoSuccess) {
            StringBuilder sb = new StringBuilder();
            NoSuccess f = (NoSuccess)pr;
            while (f != null) {
                Reader str = f.next;
                String sample = str.source().subSequence(str.offset(), Math.min(str.source().length(), 20)).toString();
                sb.append(String.format("%s(%d, %d): %s <%s>%n", f.getClass().getSimpleName(), 
                        f.next().line(), f.next().column(), f.msg, sample));
                f = f.reason;
            }
            return sb.toString();
        }
        return String.format("Unknown: [%s] %s", pr.getClass().getName(), pr);
    }
    
    public String dumpValue(Object v) {
        StringBuilder sb = new StringBuilder();
        dumpValue(v, sb);
        return sb.toString();
    }
    
    private void dumpValue(Object v, StringBuilder sb) {
        if (v instanceof List) {
            List<Object> lst = (List<Object>)v;
            boolean first = true;
            sb.append("List(");
            for (Object e: lst) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                dumpValue(e, sb);
            }
            sb.append(")");
        } else if (v instanceof Object[]) {
            Object[] arr = (Object[])v;
            boolean first = true;
            sb.append("Array(");
            for (Object e: arr) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                dumpValue(e, sb);
            }
            sb.append(")");
        } else {
            sb.append((v == null) ? "null" : v.toString());
        }
    }
    
}
