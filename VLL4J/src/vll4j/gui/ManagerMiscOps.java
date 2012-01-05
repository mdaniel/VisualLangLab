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

import java.awt.event.ActionEvent;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import vll4j.core.Utils;

public class ManagerMiscOps {

    ManagerMiscOps(Vll4jGui gui) {
        this.gui = gui;
        whiteSpace = Utils.reEscape(gui.regexParsers.getWhitespace());
    }
    
    public void setWhitespace() {
        String ws = Utils.unEscape(whiteSpace);
        String cmts = Utils.unEscape(commentSpec);
        if (ws.isEmpty() && cmts.isEmpty()) {
            gui.regexParsers.setWhitespace(Pattern.compile(""));
        } else if (cmts.isEmpty()) {
            gui.regexParsers.setWhitespace(Pattern.compile(ws));
        } else if (ws.isEmpty()) {
            gui.regexParsers.setWhitespace(Pattern.compile(cmts));
        } else {
            String wsp = String.format("(?:(?:%s)|(?:%s))+", ws, cmts);
            gui.regexParsers.setWhitespace(Pattern.compile(wsp));
        }
    }

    Action globalsWhitespaceAction = new AbstractAction("Whitespace") {
        public void actionPerformed(ActionEvent e) {
            String ws = (String)JOptionPane.showInputDialog(gui, "Enter whitespace pattern", "WhiteSpace", 
                    JOptionPane.QUESTION_MESSAGE, null, null, whiteSpace);
            if (ws == null || ws.equals(whiteSpace))
                return;
            try {
                Pattern.compile(Utils.unEscape(ws));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, ex.toString(), "ERROR - WhiteSpace", JOptionPane.ERROR_MESSAGE);
                return;
            }
            whiteSpace = ws;
            setWhitespace();
        }
    };

    Action globalsCommentAction = new AbstractAction("Comment") {
        public void actionPerformed(ActionEvent e) {
            String cmts = (String)JOptionPane.showInputDialog(gui, "Enter comment pattern", "Comments", 
                    JOptionPane.QUESTION_MESSAGE, null, null, commentSpec);
            if (cmts == null || cmts.equals(commentSpec))
                return;
            try {
                Pattern.compile(Utils.unEscape(cmts));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, ex.toString(), "ERROR - WhiteSpace", JOptionPane.ERROR_MESSAGE);
                return;
            }
            commentSpec = cmts;
            setWhitespace();
        }
    };

    private Vll4jGui gui;
    public String whiteSpace = "";
    public String commentSpec = "";
}
