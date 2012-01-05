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

public class ReaderCharSequence implements Reader {
    public ReaderCharSequence(CharSequence source, int offset) {
        this.source = source;
        this.offset = offset;
    }
    public ReaderCharSequence(CharSequence source) {
        this(source, 0);
    }
    public ReaderCharSequence(Reader r) {
        this.source = r.source();
        this.offset = r.offset();
    }
    public CharSequence source() {return source;}
    public int offset() {return offset;}
    public boolean atEnd() {return offset >= source.length();}
    public char first() {return source.charAt(offset);}
    public ReaderCharSequence rest() {
        ReaderCharSequence csr = new ReaderCharSequence(source, offset + 1);
        csr.line = line; csr.column = column;
        return csr;
    }
    public int line() {return line;}
    public int column() {return column;}
    public ReaderCharSequence drop(int n) {
        if (offset + n > source.length())
            throw new IllegalArgumentException();
        for (int i = 0; i < n; ++i) {
            if (source.charAt(offset + i) == '\n') {
                ++line;
                column = 1;
            } else {
                ++column;
            }
        }
        ReaderCharSequence csr = new ReaderCharSequence(source, offset + n);
        csr.line = line; csr.column = column;
        return csr;
    }
    private CharSequence source;
    private int offset;
    private int line = 1, column = 1;
}
