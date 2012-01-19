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

package net.java.vll.vll4j.combinator;

public class CharSequenceReader extends Reader {

    public CharSequenceReader(CharSequence source, int offset) {
        assert offset >= 0;
        this.source = source;
        this.offset = offset;
    }

    public CharSequenceReader(CharSequence source) {
        this.source = source;
        this.offset = 0;
    }

    @Override
    public CharSequence source() {
        return source;
    }

    @Override
    public int offset() {
        return offset;
    }

    @Override
    public boolean atEnd() {
        return offset >= source.length();
    }

    @Override
    public char first() {
        return (offset < source.length()) ? source.charAt(offset) : EofCh;
    }

    @Override
    public Reader rest() {
        return drop(1);
    }

    @Override
    public int line() {
        return line;
    }

    @Override
    public int column() {
        return column;
    }

    @Override
    public CharSequenceReader drop(int nbrToDrop) {
        if (offset + nbrToDrop > source.length()) {
            throw new IllegalArgumentException();
        }
        CharSequenceReader csr = new CharSequenceReader(source);
        csr.line = line;
        csr.column = column;
        csr.offset = offset + nbrToDrop;
        for (int i = 0; i < nbrToDrop; ++i) {
            if (source.charAt(offset + i) == '\n') {
                ++csr.line;
                csr.column = 1;
            } else {
                ++csr.column;
            }
        }
        return csr;
    }
    
    private CharSequence source;
    private int offset = 0;
    private int line = 1, column = 1;
    private static char EofCh = '\032';
}
