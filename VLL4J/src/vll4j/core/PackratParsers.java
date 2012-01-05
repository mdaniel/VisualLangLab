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

public class PackratParsers extends SimpleLexingRegexParsers {
    
    static class PackratReader extends ReaderCharSequence {
        PackratReader(Reader under) {
            super(under);
        }
        
    }
    
    static abstract class PackratParser<T> extends Parser<T> {
        
    }
    
    @Override
    public <T>Parser<T> phrase(Parser<T> p) {
        final Parser<T> q = super.phrase(p);
        Parser<T> parser = new PackratParser<T>() {
            public ParseResult<T> parse(Reader in) {
                if (in instanceof PackratReader)
                    return q.parse(in);
                else
                    return q.parse(new PackratReader(in));
            }
        };
        return parser;
    }
}
