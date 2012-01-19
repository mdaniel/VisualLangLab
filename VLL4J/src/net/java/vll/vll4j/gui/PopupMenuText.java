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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class PopupMenuText extends JPopupMenu {
    PopupMenuText(TextAreaCustom textComponent) {
        this.textComponent = textComponent;
        createPopup();
    }
    
    private void createPopup() {
        cutItem = new JMenuItem(cutAction);
        add(cutItem);
        copyItem = new JMenuItem(copyAction);
        add(copyItem);
        pasteItem = new JMenuItem(pasteAction);
        add(pasteItem);
        clearItem = new JMenuItem(clearAction);
        add(clearItem);
        selectAllItem = new JMenuItem(selectAllAction);
        add(selectAllItem);
    }
    
    void adjustMenu() {
    }
    
    private Action cutAction = new AbstractAction("Cut", Resources.cut16) {
        public void actionPerformed(ActionEvent evt) {
            textComponent.cut();
            textComponent.dispatchEvent(new KeyEvent(textComponent, KeyEvent.KEY_TYPED, 0, 0, 
                    KeyEvent.VK_UNDEFINED, '\033'));
        }
    };
    
    private Action copyAction = new AbstractAction("Copy", Resources.copy16) {
        public void actionPerformed(ActionEvent evt) {
            textComponent.copy();
        }
    };
    
    private Action pasteAction = new AbstractAction("Paste", Resources.paste16) {
        public void actionPerformed(ActionEvent evt) {
            textComponent.paste();
            textComponent.dispatchEvent(new KeyEvent(textComponent, KeyEvent.KEY_TYPED, 0, 0, 
                    KeyEvent.VK_UNDEFINED, '\033'));
        }
    };
    
    private Action clearAction = new AbstractAction("Clear", Resources.clear16) {
        public void actionPerformed(ActionEvent evt) {
            textComponent.setText("");
            textComponent.dispatchEvent(new KeyEvent(textComponent, KeyEvent.KEY_TYPED, 0, 0, 
                    KeyEvent.VK_UNDEFINED, '\033'));
        }
    };
    
    private Action selectAllAction = new AbstractAction("Select All") {
        public void actionPerformed(ActionEvent evt) {
            textComponent.setSelectionStart(0);
            textComponent.setSelectionEnd(textComponent.getText().length());
            textComponent.selectAll();
        }
    };
    
    TextAreaCustom textComponent;
    JMenuItem cutItem;
    JMenuItem copyItem;
    JMenuItem pasteItem;
    JMenuItem clearItem;
    JMenuItem selectAllItem;
}
