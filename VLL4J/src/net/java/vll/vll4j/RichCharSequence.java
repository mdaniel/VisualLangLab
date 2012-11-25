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

package net.java.vll.vll4j;

import java.util.Arrays;
import java.util.regex.Pattern;

public final class RichCharSequence implements CharSequence {

    public RichCharSequence(CharSequence value) {
        this.value = value;
        offset = 0;
        count = value.length();
    }

    // Package private constructor which shares value array for speed.
    RichCharSequence(int offset, int count, CharSequence value) {
        this.value = value;
        this.offset = offset;
        this.count = count;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof CharSequence) {
            CharSequence cso = (CharSequence)obj;
            if (count != cso.length())
                return false;
            for (int i = 0; i < count; ++i)
                if (charAt(i) != cso.charAt(i))
                    return false;
            return true;
        } else
            return false;
    }

    public int length() {
        return count;
    }

    public CharSequence subSequence(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > count) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if (beginIndex > endIndex) {
            throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
        }
        return ((beginIndex == 0) && (endIndex == count)) ? this :
                new RichCharSequence(offset + beginIndex, endIndex - beginIndex, value);
    }

    public char charAt(int index) {
        if (index < 0 || index >= count) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value.charAt(index + offset);
    }

    public String toString() {
        return value.subSequence(offset, offset + count).toString();
    }

    final int offset;
    final int count;
    final CharSequence value;

    /*
    The following code is inspired by the String class of JDK 7u3
     */

    public boolean matches(String regex) {
        return Pattern.matches(regex, this);
    }

    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    public int indexOf(String str, int fromIndex) {
        outer:
        for (int i = offset + fromIndex; i < offset + count - str.length(); ++i) {
            for (int j = 0; j < str.length(); ++j) {
                if (str.charAt(j) != value.charAt(i))
                    continue outer;
            }
            return i - offset;
        }
        return -1;
    }

    public boolean startsWith(String prefix, int toffset) {
        int to = offset + toffset;
        int po = 0;
        int pc = prefix.length(); //prefix.count;
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > count - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (value.charAt(to++) != prefix.charAt(po++)) {
                return false;
            }
        }
        return true;
    }

    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    public boolean endsWith(String suffix) {
        return startsWith(suffix, count - suffix.length());
    }

    public char[] toCharArray() {
        return toString().toCharArray();
    }

    public int hashCode() {
        int h = hashValue;
        if (h == 0 && count > 0) {
            int off = offset;
            int len = count;

            for (int i = 0; i < len; i++) {
                h = (31 * h) + value.charAt(off++);
            }
            hashValue = h;
        }
        return h;
    }

    private int hashValue;
}
