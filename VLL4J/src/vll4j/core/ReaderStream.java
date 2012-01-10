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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import vll4j.core.Parsers.Reader;

public class ReaderStream implements Reader {
    public ReaderStream(InputStream inputStream) {
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            isr = new InputStreamReader(inputStream);
            br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            while ((buffer = br.readLine()) != null)
                sb.append(buffer).append('\n');
            buffer = sb.toString();
        } catch (Exception ex) {
        } finally {
            try {isr.close();} catch (IOException ex) {}
            try {br.close();} catch (IOException ex) {}
        }
    }
    
    private ReaderStream(String b) {
        buffer = b;
    }
    
    @Override
    public CharSequence source() {return buffer;}
    @Override
    public int offset() {return offset;}
    @Override
    public boolean atEnd() {return offset >= buffer.length();}
    @Override
    public char first() {return buffer.charAt(offset);}
    @Override
    public ReaderStream rest() {
        return drop(1);
    }
    @Override
    public int line() {return line;}
    @Override
    public int column() {return column;}
    @Override
    public ReaderStream drop(int nbrToDrop) {
        String src = buffer;
        if (offset + nbrToDrop > src.length())
            throw new IllegalArgumentException();
        ReaderStream csr = new ReaderStream(buffer);
        csr.offset = offset + nbrToDrop; csr.line = line; csr.column = column;
        for (int i = 0; i < nbrToDrop; ++i) {
            if (src.charAt(offset + i) == '\n') {
                ++csr.line;
                csr.column = 1;
            } else {
                ++csr.column;
            }
        }
        return csr;
    }
    
    private int offset = 0;
    private int line = 1, column = 1;
    private String buffer;
}
