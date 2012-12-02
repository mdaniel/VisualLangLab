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

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class LogTextArea extends JTextArea {
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (fontMetrics == null) {
            fontMetrics = g.getFontMetrics();
            fontBase = fontMetrics.getLeading() + fontMetrics.getAscent();
            fontHeight = fontMetrics.getHeight();
            backGroundColor = getBackground();
        }
        int y1 = getVisibleRect().y;
        int y2 = y1 + getVisibleRect().height;
        int width = getVisibleRect().width;
        int rowHeight = getRowHeight();
        for (Integer[] el: errLines) {
            try {
                int textOffset = el[0];
                int textLength = el[1];
                int y = modelToView(textOffset).y;
                if (y + rowHeight >= y1 && y <= y2) {
                    String s = getText(textOffset, textLength);
                    outer:
                    while (getLineWrap() && (s != null) && !s.isEmpty()) {
                        int len = 0;
                        for (int j = 0; j < s.length(); ++j) {
                            len += fontMetrics.charWidth(s.charAt(j));
                            if (len > width) {
                                String ss = s.substring(0, j);
                                g.setColor(backGroundColor);
                                g.fillRect(0, y, width, fontHeight);
                                g.setColor(Color.red);
                                g.drawString(ss, 0, y + fontBase);
                                y += rowHeight;
                                s = s.substring(j);
                                continue outer;
                            }
                        }
                        break;
                    }
                    if (!s.isEmpty()) {
                        g.setColor(backGroundColor);
                        g.fillRect(0, y, width, fontHeight);
                        g.setColor(Color.red);
                        g.drawString(s, 0, y + fontBase);
                    }
                }
            } catch (Exception e) {}
        }
    }
    volatile ArrayList<Integer[]> errLines = new ArrayList<Integer[]>();
    private Color backGroundColor;
    private int fontHeight, fontBase;
    private FontMetrics fontMetrics = null;
}
