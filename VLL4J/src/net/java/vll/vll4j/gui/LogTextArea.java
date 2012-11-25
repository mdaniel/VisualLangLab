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
        int y1 = getVisibleRect().y;
        int y2 = y1 + getVisibleRect().height;
        int width = getVisibleRect().width;
        int rowHeight = getRowHeight();
        int fontBase = g.getFontMetrics().getLeading() + g.getFontMetrics().getAscent();
        Color bgColor = getBackground();
        for (Object[] el: errLines) {
            int y = rowHeight * (Integer)el[0];
            if (y + rowHeight >= y1 && y <= y2) {
                g.setColor(bgColor);
                g.fillRect(0, y, width, rowHeight);
                g.setColor(Color.red);
                g.drawString((String)el[1], 0, y + fontBase);
            }
        }
    }
    volatile ArrayList<Object[]> errLines = new ArrayList<Object[]>();
}
