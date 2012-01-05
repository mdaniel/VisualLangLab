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

import javax.swing.ImageIcon;

public class Resources {
    static ClassLoader cl = ClassLoader.getSystemClassLoader();
    static ImageIcon getIcon(String name) {
        return new ImageIcon(cl.getResource("vll4j/gui/resources/" + name));
    }
    static ImageIcon alignLeft16 = getIcon("AlignLeft16.gif");
    static ImageIcon back16 = getIcon("Back16.gif");
    static ImageIcon choice = getIcon("Choice.gif");
    static ImageIcon clear16 = getIcon("Clear16.gif");
    static ImageIcon commitMark = getIcon("CommitMark.gif");
    static ImageIcon copy16 = getIcon("Copy16.gif");
    static ImageIcon cut16 = getIcon("Cut16.gif");
    static ImageIcon delete16 = getIcon("Delete16.gif");
    static ImageIcon edit16 = getIcon("Edit16.gif");
    static ImageIcon errorMark = getIcon("ErrorMark.gif");
    static ImageIcon export16 = getIcon("Export16.gif");
    static ImageIcon fastForward16 = getIcon("FastForward16.gif");
    static ImageIcon host16 = getIcon("Host16.gif");
    static ImageIcon icon = getIcon("Icon.gif");
    static ImageIcon import16 = getIcon("Import16.gif");
    static ImageIcon literal = getIcon("Literal.gif");
    static ImageIcon new16 = getIcon("New16.gif");
    static ImageIcon newLiteral = getIcon("NewLiteral.gif");
    static ImageIcon newReference = getIcon("NewReference.gif");
    static ImageIcon newRegex = getIcon("NewRegex.gif");
    static ImageIcon open16 = getIcon("Open16.gif");
    static ImageIcon paste16 = getIcon("Paste16.gif");
    static ImageIcon play16 = getIcon("Play16.gif");
    static ImageIcon predicate = getIcon("SemPred.gif");
    static ImageIcon reference = getIcon("Reference.gif");
    static ImageIcon refresh16 = getIcon("Refresh16.gif");
    static ImageIcon regex = getIcon("Regex.gif");
    static ImageIcon repSep = getIcon("RepSep.gif");
    static ImageIcon replace16 = getIcon("Replace16.gif");
    static ImageIcon root = getIcon("Root.gif");
    static ImageIcon save16 = getIcon("Save16.gif");
    static ImageIcon saveAs16 = getIcon("SaveAs16.gif");
    static ImageIcon search16 = getIcon("Search16.gif");
    static ImageIcon semPred = getIcon("SemPred.gif");
    static ImageIcon sequence = getIcon("Sequence.gif");
    static ImageIcon server16 = getIcon("Server16.gif");
    static ImageIcon stop16 = getIcon("Stop16.gif");
    static ImageIcon token = getIcon("Token.gif");
    static ImageIcon wildCard = getIcon("WildCard.gif"); 
}
