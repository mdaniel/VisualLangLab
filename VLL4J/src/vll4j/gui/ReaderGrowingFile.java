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
import vll4j.core.Reader;

public class ReaderGrowingFile extends Reader {
    public ReaderGrowingFile(File theFile) {
        cache = new Cache(theFile);
    }
    
    private ReaderGrowingFile(Cache c) {
        cache = c;
    }
    
    private static class Cache {
        Cache(File theFile) {
            if (!theFile.exists())
                throw new IllegalArgumentException("Nonexistent file");
            this.theFile = theFile;
            refresh();
        }
        
        String getBuffer() {
            refresh();
            return buffer;
        }
        
        private void refresh() {
            long lastMod = theFile.lastModified();
            if (lastMod == lastModified)
                return;
            lastModified = lastMod;
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(theFile));
                StringBuilder sb = new StringBuilder();
                String line; 
                while ((line = br.readLine()) != null)
                    sb.append(line);
                buffer = sb.toString();
            } catch (Exception ex) {
            } finally {
                try {br.close();} catch (IOException ex) {}
            }
        }
    
        private File theFile;
        private long lastModified = 0;
        private String buffer;
    }
    @Override
    public CharSequence source() {return cache.getBuffer();}
    @Override
    public int offset() {return offset;}
    @Override
    public boolean atEnd() {return offset >= cache.getBuffer().length();}
    @Override
    public char first() {return cache.getBuffer().charAt(offset);}
    @Override
    public ReaderGrowingFile rest() {
        return drop(1);
    }
    @Override
    public int line() {return line;}
    @Override
    public int column() {return column;}
    @Override
    public ReaderGrowingFile drop(int nbrToDrop) {
        String src = cache.getBuffer();
        if (offset + nbrToDrop > src.length())
            throw new IllegalArgumentException();
        ReaderGrowingFile csr = new ReaderGrowingFile(cache);
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
    private Cache cache = null;
}
