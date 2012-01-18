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

package net.java.vll.vllj.gui;

import java.awt.event.ActionEvent;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import net.java.vll.vllj.core.Utils;

public class ManagerMiscOps {

    ManagerMiscOps(Vll4jGui gui) {
        this.gui = gui;
    }
    
    Action globalsWhitespaceAction = new AbstractAction("Whitespace") {
        public void actionPerformed(ActionEvent e) {
            String ws = (String)JOptionPane.showInputDialog(gui, "Enter whitespace pattern", "WhiteSpace", 
                    JOptionPane.QUESTION_MESSAGE, null, null, gui.regexParsers.whiteSpaceRegex);
            if (ws == null || ws.equals(gui.regexParsers.whiteSpaceRegex))
                return;
            try {
                Pattern.compile(Utils.unEscape(ws));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, ex.toString(), "ERROR - WhiteSpace", JOptionPane.ERROR_MESSAGE);
                return;
            }
            gui.regexParsers.whiteSpaceRegex = ws;
            gui.regexParsers.resetWhitespace();
        }
    };

    Action globalsCommentAction = new AbstractAction("Comment") {
        public void actionPerformed(ActionEvent e) {
            String cmts = (String)JOptionPane.showInputDialog(gui, "Enter comment pattern", "Comments", 
                    JOptionPane.QUESTION_MESSAGE, null, null, gui.regexParsers.commentSpecRegex);
            if (cmts == null || cmts.equals(gui.regexParsers.commentSpecRegex))
                return;
            try {
                Pattern.compile(Utils.unEscape(cmts));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(gui, ex.toString(), "ERROR - WhiteSpace", JOptionPane.ERROR_MESSAGE);
                return;
            }
            gui.regexParsers.commentSpecRegex = cmts;
            gui.regexParsers.resetWhitespace();
        }
    };

    private Vll4jGui gui;
}
