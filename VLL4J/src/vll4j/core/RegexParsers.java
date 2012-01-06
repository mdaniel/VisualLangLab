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
            public ParseResult<String> parse(Reader input) {
                int offset2 = handleWhiteSpace(input.source(), input.offset());
                Reader newInput = input.drop(offset2 - input.offset());
                CharSequence cs = newInput.source();
                if (cs.subSequence(offset2, cs.length()).toString().startsWith(lit))
                    return new Success(lit, newInput.drop(lit.length()));
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
            public ParseResult<String> parse(Reader input) {
                int offset2 = handleWhiteSpace(input.source(), input.offset());
                Reader newInput = input.drop(offset2 - input.offset());
                CharSequence cs = newInput.source();
                Matcher m = p.matcher(cs.subSequence(newInput.offset(), cs.length()));
                if (m.lookingAt()) {
                    return new Success(m.group(), newInput.drop(m.group().length()));
                } else
                    return new Failure(errMsg, input);
            }
        };
    }
    
    public Parser<String> regex(String errMsg, String pat) {
        return regex(errMsg, Pattern.compile(pat));
    }
    
    public Parser<String> regex(String pat) {
        return regex(Pattern.compile(pat));
    }
    
    @Override
    public <T>Parser<T> phrase(final Parser<T> p) {
        return new Parser<T>() {
            public ParseResult<T> parse(Reader input) {
                ParseResult<T> pr = p.parse(input);
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
        return phrase(p).parse(new ReaderCharSequence(cs));
    }
    
    public ParseResult<? extends Object> parseAll(Parser<? extends Object> p, Reader rdr) {
        return phrase(p).parse(rdr);
    }
    
}
