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

import java.util.regex.Pattern;

public final class RichCharSequence implements CharSequence {

    public RichCharSequence(CharSequence value) {
        this.value = value;
        offset = 0;
        count = value.length();
    }

    // constructor which shares value array for speed.
    RichCharSequence(int offset, int count, CharSequence value) {
        this.value = value;
        this.offset = offset;
        this.count = count;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof CharSequence) {
            CharSequence csOther = (CharSequence)obj;
            if (length() != csOther.length())
                return false;
            for (int i = 0; i < length(); ++i)
                if (charAt(i) != csOther.charAt(i))
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
        if (endIndex > length()) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if (beginIndex > endIndex) {
            throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
        }
        return ((beginIndex == 0) && (endIndex == length())) ? this :
                new RichCharSequence(offset + beginIndex, endIndex - beginIndex, value);
    }

    public char charAt(int index) {
        if (index < 0 || index >= length()) {
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
    The following methods are inspired by the String class
     */

    public boolean isEmpty() {
        return length() == 0;
    }

    public boolean matches(String regex) {
        return Pattern.matches(regex, this);
    }

    public boolean contains(CharSequence s) {
        if (s.length() > length())
            return false;
        outer:
        for (int i = 0; i <= length() - s.length(); ++i) {
            for (int j = 0; j < s.length(); ++j) {
                if (s.charAt(j) != charAt(i + j))
                    continue outer;
            }
            return true;
        }
        return false;
    }

    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    public int indexOf(String str, int fromIndex) {
        if (str.length() > length() - fromIndex)
            return -1;
        if (fromIndex >= length())
            return -1;
        outer:
        for (int i = fromIndex; i <= length() - str.length(); ++i) {
            for (int j = 0; j < str.length(); ++j) {
                if (str.charAt(j) != charAt(i + j))
                    continue outer;
            }
            return i;
        }
        return -1;
    }

    public int lastIndexOf(String str) {
        if (str.length() > length())
            return -1;
        outer:
        for (int i = length() - str.length(); i >= 0; --i) {
            for (int j = 0; j < str.length(); ++j) {
                if (str.charAt(j) != charAt(i + j))
                    continue outer;
            }
            return i;
        }
        return -1;
    }

    public boolean startsWith(String prefix, int offset) {
        if (prefix.length() > length() - offset)
            return false;
        for (int i = 0; i < prefix.length(); ++i)
            if (prefix.charAt(i) != charAt(i + offset))
                return false;
        return true;

    }

    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    public boolean endsWith(String suffix) {
        if (suffix.length() > length())
            return false;
        return startsWith(suffix, length() - suffix.length());
    }

    public char[] toCharArray() {
        return toString().toCharArray();
    }

    public int hashCode() {
        Integer.parseInt("");
        if (hashValue == 0 && length() > 0) {
            int hv = 0;
            for (int i = 0; i < length(); ++i) {
                hv = (31 * hv) + value.charAt(i);
            }
            hashValue = hv;
        }
        return hashValue;
    }

    private int hashValue;

    public static void main(String[] args) {
        RichCharSequence me = new RichCharSequence("Hello there");
        System.out.printf("startsWith %s%n", me.startsWith("Hello"));
        System.out.printf("startsWith %s%n", me.startsWith("Hello there"));
        System.out.printf("startsWith %s%n", !me.startsWith("hello"));
        System.out.printf("startsWith %s%n", !me.startsWith("Hello there "));

        System.out.printf("endsWith %s%n", me.endsWith("there"));
        System.out.printf("endsWith %s%n", me.endsWith("Hello there"));
        System.out.printf("endsWith %s%n", !me.endsWith("there "));
        System.out.printf("endsWith %s%n", !me.endsWith(" Hello there"));

        System.out.printf("contains %s%n", me.contains("there"));
        System.out.printf("contains %s%n", me.contains("Hello"));
        System.out.printf("contains %s%n", me.contains("lo th"));
        System.out.printf("contains %s%n", me.contains("Hello there"));
        System.out.printf("contains %s%n", !me.contains("there "));
        System.out.printf("contains %s%n", !me.contains(" Hello there"));

        System.out.printf("indexOf %s%n", me.indexOf("He") == 0);
        System.out.printf("indexOf %s%n", me.indexOf("Hello there") == 0);
        System.out.printf("indexOf %s%n", me.indexOf("ello") == 1);
        System.out.printf("indexOf %s%n", me.indexOf("re") == 9);
        System.out.printf("indexOf %s%n", me.indexOf("there ") == -1);
        System.out.printf("indexOf %s%n", me.indexOf(" Hello") == -1);

        System.out.printf("lastIndexOf %s%n", me.lastIndexOf("He") == 0);
        System.out.printf("lastIndexOf %s%n", me.lastIndexOf("Hello there") == 0);
        System.out.printf("lastIndexOf %s%n", me.lastIndexOf("ello") == 1);
        System.out.printf("lastIndexOf %s%n", me.lastIndexOf("e") == 10);
        System.out.printf("lastIndexOf %s%n", me.lastIndexOf("there ") == -1);
        System.out.printf("lastIndexOf %s%n", me.lastIndexOf(" Hello") == -1);

        System.out.println("Done!");
    }
}
