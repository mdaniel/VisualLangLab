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
 The design and code of this class is inspired by the element of the 
 same name in the Scala (http://scala-lang.org/) standard library. 
 The Scala license is reproduced in the ZIP and JAR distributions.
 */

package net.java.vll.vll4j.combinator;

public abstract class Reader {

    public CharSequence source() {
        throw new NoSuchMethodError("not a char sequence reader");
    }

    public abstract boolean atEnd();

    public abstract char first();

    public abstract Reader rest();

    public int offset() {
        throw new NoSuchMethodError("not a char sequence reader");
    }
    
    public abstract int line();

    public abstract int column();

    public Reader drop(int n) {
        Reader r = this;
        int cnt = n;
        while (cnt > 0) {
            r = r.rest(); cnt -= 1;
        }
        return r;
    }
}
