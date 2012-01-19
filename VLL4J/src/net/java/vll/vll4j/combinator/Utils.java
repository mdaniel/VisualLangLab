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

package net.java.vll.vll4j.combinator;

import java.util.HashMap;

public class Utils {
    
    private static HashMap<String, Character> decodeMap = new HashMap<String, Character>();
    static {
        String keys[] = new String[] {"&#xA;", "&#x9;", "&#xD;", "&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};
        char vals[] = new char[] {'\n', '\t', '\r', '&', '<', '>', '\"', '\''};
        for (int i = 0; i < keys.length; ++i)
            decodeMap.put(keys[i], vals[i]);
    }
    
    public static String encode4xml(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\n': sb.append("&#xA;"); break;
                case '\t': sb.append("&#x9;"); break;
                case '\r': sb.append("&#xD;"); break;
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '\"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
                default: sb.append(ch); break;
            }
        }
        return sb.toString();
    }
    
    public static String decode4xml(String s) {
System.out.printf("decode(%s)=", s);
        StringBuilder sb = new StringBuilder();
        StringBuilder esc = new StringBuilder();
        boolean escaping = false;
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            if (escaping) {
                esc.append(ch);
                if (ch == ';') {
                    escaping = false;
                    sb.append(decodeMap.get(esc.toString()));
                    esc.setLength(0);
                }
            } else {
                if (ch == '&') {
                    escaping = true;
                    esc.append(ch);
                } else {
                    sb.append(ch);
                }
            }
        }
System.out.println(sb.toString());
        return sb.toString();
    }
    
    public static String escapeMetachars(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            switch (ch) {
                case '(': case ')': case '[': case ']': case '{': case '}': 
                case '*': case '+': case '|': case '?': case '-': 
                case ',': case '.': case '^': case '$': case '\\': sb.append('\\').append(ch);
                    break;
                default: sb.append(ch);
            }
        }
        return sb.toString();
    }
    
    public static String unEscape(String s) {
        if (!s.contains("\\"))
            return s;
        else {
            StringBuilder sb = new StringBuilder();
            boolean escaping = false;
            for (int i = 0; i < s.length(); ++i) {
                char ch = s.charAt(i);
                if (escaping) {
                    escaping = false;
                    switch(s.charAt(i)) {
                        case 'b' : sb.append('\b'); break;
                        case 'f' : sb.append('\f'); break;
                        case 'n' : sb.append('\n'); break;
                        case 'r' : sb.append('\r'); break;
                        case 't' : sb.append('\t'); break;
                        case '\\' : sb.append('\\'); break;
                        case '\'' : sb.append('\''); break;
                        case '\"' : sb.append('\"'); break;
                        default: throw new IllegalArgumentException(
                                String.format("Bad escape at offset %d -> %s", i, s));
                    }
                } else {
                    if (ch == '\\')
                        escaping = true;
                    else
                        sb.append(ch);
                }
            }
            return sb.toString();
        }
    }
    
    public static String reEscape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\\': sb.append("\\\\"); break;
                default: sb.append(ch);
            }
        }
        return sb.toString();
    }
}
