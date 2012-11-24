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
