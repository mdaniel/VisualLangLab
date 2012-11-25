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

package net.java.vll.vll4j.gui;

import javax.swing.JTextArea;

import net.java.vll.vll4j.RichCharSequence;
import net.java.vll.vll4j.combinator.Reader;

public class ReaderTextArea extends Reader {
    public ReaderTextArea(JTextArea textArea, boolean useRichCharSequence) {
        this.textArea = textArea;
        this.useRichCharSequence = useRichCharSequence;
        source = useRichCharSequence ? new RichCharSequence(textArea.getText()) : textArea.getText();
    }
    @Override
    public CharSequence source() {
        if (useRichCharSequence) {
            if (source.equals(textArea.getText()))
                return source;
            else {
                source = new RichCharSequence(textArea.getText());
                return source;
            }
        } else
            return textArea.getText();
    }
    @Override
    public int offset() {return offset;}
    @Override
    public boolean atEnd() {return offset >= textArea.getText().length();}
    @Override
    public char first() {return textArea.getText().charAt(offset);}
    @Override
    public ReaderTextArea rest() {
        return drop(1);
    }
    @Override
    public int line() {return line;}
    @Override
    public int column() {return column;}
    @Override
    public ReaderTextArea drop(int nbrToDrop) {
        if (offset + nbrToDrop > textArea.getText().length())
            throw new IllegalArgumentException();
        ReaderTextArea csr = new ReaderTextArea(textArea, useRichCharSequence);
        csr.offset = offset + nbrToDrop; csr.line = line; csr.column = column;
        String src = textArea.getText();
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
    private boolean useRichCharSequence;
    private JTextArea textArea;
    private int offset = 0;
    private int line = 1, column = 1;
    private CharSequence source;
}
