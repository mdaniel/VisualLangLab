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

package vll4j.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import vll4j.core.Parsers.Reader;

public class ReaderFile implements Reader {
    public ReaderFile(File theFile) {
        if (!theFile.exists())
            throw new IllegalArgumentException("Nonexistent file");
        BufferedReader br = null;
        FileReader fr = null;
        try {
            fr = new FileReader(theFile);
            br = new BufferedReader(fr);
            StringBuilder sb = new StringBuilder();
            while ((buffer = br.readLine()) != null)
                sb.append(buffer).append('\n');
            buffer = sb.toString();
        } catch (Exception ex) {
        } finally {
            try {fr.close();} catch (IOException ex) {}
            try {br.close();} catch (IOException ex) {}
        }
    }
    
    private ReaderFile(String b) {
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
    public ReaderFile rest() {
        return drop(1);
    }
    @Override
    public int line() {return line;}
    @Override
    public int column() {return column;}
    @Override
    public ReaderFile drop(int nbrToDrop) {
        String src = buffer;
        if (offset + nbrToDrop > src.length())
            throw new IllegalArgumentException();
        ReaderFile csr = new ReaderFile(buffer);
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
