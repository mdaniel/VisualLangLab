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

import java.awt.Font;
import javax.swing.JTextArea;

public class TextAreaCustom extends JTextArea {
    
    TextAreaCustom() {
        setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
        addMouseListener(new PopupListenerText(this));
    }
    
    PopupMenuText popupMenuText = new PopupMenuText(this);
}
