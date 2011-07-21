/*
 Copyright 2010, Sanjay Dasgupta
 sanjay.dasgupta@gmail.com

 This file is part of VisualLangLab (https://vll.dev.java.net/).

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

package vll.gui

//import dk.brics.automaton.RegExp
import java.awt.Cursor
import java.awt.Dimension
import scala.swing.Dialog
import scala.swing.FileChooser
import java.awt.Point
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.io.IOException
import java.io.PrintStream
import java.net.URL
import java.net.URLClassLoader
import javax.swing.JComboBox
import javax.swing.JToolBar
import javax.swing.SwingUtilities
import javax.swing.UIManager
import javax.swing.filechooser.FileFilter
import scala.swing.Action
import scala.swing.BorderPanel
import scala.swing.Button
import scala.swing.ButtonGroup
import scala.swing.CheckMenuItem
import scala.swing.Component
import scala.swing.MainFrame
import scala.swing.Menu
import scala.swing.MenuBar
import scala.swing.MenuItem
import scala.swing.Orientation
import scala.swing.RadioMenuItem
import scala.swing.Separator
import scala.swing.SimpleSwingApplication
import scala.swing.SplitPane
import scala.swing.event.ButtonClicked
import vll.core.RootNode
import vll.core.Automata
import vll.core.ScalaEngine
import vll.core.Utils
import vll.gui.samples.{ArithExpr, ArithExprWithActionCode, SimpleJSON}

class VllGui extends MainFrame with ActionListener {

  try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
  } catch  {
    case e: Throwable => e.printStackTrace()
  }

  def saveChanges() {
    if (isDirty) {
      val ok = Dialog.showConfirmation(splitPane, "Save changes?", "Unsaved changes exist",
          Dialog.Options.YesNoCancel, Dialog.Message.Question, null)
      ok match {
        case Dialog.Result.Yes =>
          grammarFile match {
            case Some(file) => parsers.save(file)
            grammarFileChooser.title = "Save"
            case None => grammarFileChooser.showSaveDialog(splitPane) match {
                case FileChooser.Result.Approve =>
                  grammarFile = Some(grammarFileChooser.selectedFile)
                  parsers.save(grammarFile.get)
                  isDirty = false
                case _ =>
              }
          }
        case Dialog.Result.Cancel =>
        case Dialog.Result.No =>
          isDirty = false
      }
    }
  }
  
  val fileNewAction = new Action("New") {
    icon = swing.Swing.Icon(getClass.getResource("images/New16.gif")) 
    toolTip = title
    def apply {
      saveChanges()
      if (!isDirty) {
        GuiNode.clearCache()
        parsers.reset()
        parserTreePanel.setParser("Main")
        updateParserChooser("Main")
        grammarFile = None
        mainFrame.title = "VisualLangLab/S"
      }
      customTreeHandlerClassName = None
      customTreeHandler = None
      runHandlerBasicMenuItem.selected = true
    }
  }
  
  val fileOpenAction = new Action("Open") {
    icon = swing.Swing.Icon(getClass.getResource("images/Open16.gif"))
    toolTip = title
    def apply {
      saveChanges()
      if (!isDirty) {
      grammarFileChooser.title = "Open"
        grammarFileChooser.showOpenDialog(splitPane) match {
          case FileChooser.Result.Approve =>
            grammarFile = Some(grammarFileChooser.selectedFile)
              //FileIO.load(grammarFile.get)
            try {
              parsers.load(grammarFile.get)
            } catch {
              case e: IOException => val sb = new StringBuilder(); sb.append("Missing ")
/*                   if (!FileIO.missingParsers.isEmpty) sb.append(FileIO.missingParsers.mkString("Parsers(", ", ", ") "))
                  if (!FileIO.missingTokens.isEmpty) sb.append(FileIO.missingTokens.mkString("Tokens(", ", ", ") "))
 */                  Dialog.showMessage(splitPane, sb.toString, "File open", Dialog.Message.Error, null)
            }
//            globalFlattenTilde.selected = parsers.flattenNestedTilde
            //println("flattens: %b, %d %n", globalFlattenTilde.selected, parsers.flattenNestedTilde)
            val firstParser = parsers.parserBank.parserNames(0)
            updateParserChooser(firstParser)
            parserTreePanel.setParser(firstParser)
            mainFrame.title = "VisualLangLab/S - " + grammarFile.get.getName
          case _ =>
        }
      }
    }
  }
  
  val fileSaveAction = new Action("Save") {
    icon = swing.Swing.Icon(getClass.getResource("images/Save16.gif"))
    toolTip = title
    def apply {
        grammarFile match {
          case Some(file) => parsers.save(file)
          case None => grammarFileChooser.showSaveDialog(splitPane) match {
              case FileChooser.Result.Approve =>
                grammarFile = Some(grammarFileChooser.selectedFile)
                parsers.save(grammarFile.get)
                mainFrame.title = "VisualLangLab/S - " + grammarFile.get.getName
              case _ =>
            }
        }
        isDirty = false
    }
  }

  val fileSaveAsAction = new Action("Save As") {
    icon = swing.Swing.Icon(getClass.getResource("images/SaveAs16.gif"))
    toolTip = title
    def apply {
        grammarFileChooser.title = "Save as"
        grammarFileChooser.showSaveDialog(splitPane) match {
          case FileChooser.Result.Approve =>
            if (grammarFileChooser.selectedFile.exists) {
              val overwrite = Dialog.showConfirmation(splitPane, "File exists. Overwrite?",
                  "Save as", Dialog.Options.YesNo, Dialog.Message.Question, null)
              if (overwrite != Dialog.Result.Yes)
                return
            }
            grammarFile = Some(grammarFileChooser.selectedFile)
            parsers.save(grammarFile.get)
            isDirty = false
            mainFrame.title = "VisualLangLab/S - " + grammarFile.get.getName
          case _ =>
        }
    }
  }

  val fileExitAction = new Action("Exit") {
    def apply {
      saveChanges()
      sys.exit()
    }
  }

  val fileMenu = new Menu("File") {
    contents.append(new MenuItem(fileNewAction), new Separator, new MenuItem(fileOpenAction), 
    new MenuItem(fileSaveAction),
    new MenuItem(fileSaveAsAction), new Separator, new MenuItem(fileExitAction))
  }

  val viewFullNamesMenuItem: CheckMenuItem = new CheckMenuItem("Full names") {
    reactions += {case ButtonClicked(_) =>
//        parserTreePanel.rootNode.fullName = viewFullNamesMenuItem.selected
        parserTreePanel.peer.repaint()
        //ParserTreePanel.theTree.treeDidChange()
        parserTreePanel.theModel.nodeStructureChanged(parserTreePanel.rootNode)
        for (i <- 0 until 20)
          parserTreePanel.theTree.expandRow(i)
        //ParserTreePanel.theModel.nodeStructureChanged(ParserTreePanel.rootNode)
    }
  }

  val viewShowEpsOkMenuItem: CheckMenuItem = new CheckMenuItem("Show \u03b5-ok items") {
    reactions += {case ButtonClicked(_) => 
//        parserTreePanel.rootNode.fullName = viewFullNamesMenuItem.selected
        parserTreePanel.peer.repaint()
        //ParserTreePanel.theTree.treeDidChange()
        parserTreePanel.theModel.nodeStructureChanged(parserTreePanel.rootNode)
        for (i <- 0 until 20)
          parserTreePanel.theTree.expandRow(i)
        //ParserTreePanel.theModel.nodeStructureChanged(ParserTreePanel.rootNode)
    }
  }
  
  val viewLookNFeelAction = new Action("Look&Feel") {
    def apply {
      val currentLaf = UIManager.getLookAndFeel()
      val lInfo = UIManager.getInstalledLookAndFeels();
      val lafNames = new Array[String](lInfo.length)
      for (i <- 0 until lInfo.length) lafNames(i) = lInfo(i).getClassName()
      Dialog.showInput(splitPane, "Choose L&F", "Look & Feel", Dialog.Message.Question, null, lafNames, currentLaf.getClass.getName) match {
        case Some(chosenLaf) =>
          try {
            UIManager.setLookAndFeel(chosenLaf)
            SwingUtilities.updateComponentTreeUI(mainFrame.peer);
          } catch {case e: Throwable => System.err.println(e)}
        case _ =>
      }
    }
  }
  
  val viewMenu = new Menu("View") {
    contents.append(viewFullNamesMenuItem, viewShowEpsOkMenuItem, new MenuItem(viewLookNFeelAction))
  }
  
  val parserNewAction = new Action("New parser") {
    icon = swing.Swing.Icon(getClass.getResource("images/NewReference.gif"))
    toolTip = title
    def apply {
    val pattern = """\s*([a-zA-Z_]\w*)\s*""".r
    Dialog.showInput(splitPane, "Enter parser-name", "New parser", Dialog.Message.Question, null, Array[String](), null) match {
      case Some(name) =>
        name match {
          case pattern(newParserName) =>
            if (parsers.parserBank contains newParserName) {
              Dialog.showMessage(splitPane, "A parser named '" + newParserName + "' already exists", "New parser", Dialog.Message.Error, null)
            } else {
              val newRoot = /* new  */RootNode(newParserName)
//              ParserTreePanel.displayStack.push(ParserTreePanel.rootNode.nodeName)
              parsers.parserBank(newParserName) = newRoot
              parserTreePanel.setParser(newParserName)
              updateParserChooser(newParserName)
              //VisualLangLab.parserChooser.setSelectedItem(newParserName)
              isDirty = true
              currentParserName = newParserName
            }
          case _ =>
            Dialog.showMessage(splitPane, "Bad parser name - identifier expected", "New parser", Dialog.Message.Error, null)
        }
      case None =>
    }
    }
  }
  
  val parserRenameAction = new Action("Rename parser") {
    icon = swing.Swing.Icon(getClass.getResource("images/Refresh16.gif"))
    toolTip = title
    def apply {
    val currentName = parserChooser.getSelectedItem.asInstanceOf[String]
    val pattern = """\s*([a-zA-Z_]\w*)\s*""".r
    Dialog.showInput(splitPane, "Enter new name", "Rename parser", Dialog.Message.Question, null, Array[String](), currentName) match {
      case Some(name) =>
        name match {
          case pattern(newParserName) =>
            if (parsers.parserBank contains newParserName) {
              Dialog.showMessage(splitPane, "A parser named '" + newParserName + "' already exists", "Rename parser", Dialog.Message.Error, null)
            } else {
              //printf("Renaming %s to %s%n", parserChooser.getSelectedItem.asInstanceOf[String], newParserName)
              parsers.parserBank.rename(currentName, newParserName)
              parserTreePanel.setParser(newParserName)
              updateParserChooser(newParserName)
              isDirty = true
              currentParserName = newParserName
            }
          case _ =>
            Dialog.showMessage(splitPane, "Bad parser name - identifier expected", "New parser", Dialog.Message.Error, null)
        }
      case None =>
    }
    }
  }
  
  val parserDeleteAction = new Action("Delete parser") {
    toolTip = title
    icon = swing.Swing.Icon(getClass.getResource("images/Delete16.gif"))
    def apply {
    def purgeStack(p: String) {
      var lst = List[String]()
      while(!parserTreePanel.displayStack.isEmpty)
        lst ::= parserTreePanel.displayStack.pop
      lst.filter(_ != p).foreach(parserTreePanel.displayStack.push)
    }
    val parserToDelete = parserChooser.getSelectedItem.asInstanceOf[String]
    val ok = Dialog.showConfirmation(splitPane, "Delete '" + parserToDelete + "' ?",
        "Delete parser", Dialog.Options.YesNo, Dialog.Message.Question, null)
    if (ok == Dialog.Result.Yes) {
      val parserNames = parsers.parserBank.parserNames //parsers.keysIterator.toSeq
      if (parserNames.size == 1) {
        Dialog.showMessage(splitPane, "Can't delete last parser (use rename instead)",
            "Delete parser", Dialog.Message.Error, null)
      } else {
        parsers.parserBank.parserInUse(parserToDelete, true) match {
          case Nil =>
            val idx = parserNames.indexOf(parserToDelete)
              parsers.parserBank -= parserToDelete
              val newDisplay = if (idx == parserNames.size - 1)
                parserNames(idx - 1) else parserNames(idx + 1)
              updateParserChooser(newDisplay)
              parserTreePanel.setParser(newDisplay)
              purgeStack(parserToDelete)
              isDirty = true
          case users: Seq[String] =>
                val msg = "Can't delete '%s' - used by: \n%s".format(parserToDelete, users.mkString(",\n"))
                Dialog.showMessage(splitPane, msg, "Delete parser", Dialog.Message.Error, null)
        }
      }
    }
    }
  }
  
  val parserReferencesAction = new Action("Find parser") {
    toolTip = title
    icon = swing.Swing.Icon(getClass.getResource("images/Search16.gif"))
    def apply {
    val defaultCursor = /* VllGui.top. */cursor
    /* VllGui.top. */cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    val parserName = parserChooser.getSelectedItem.asInstanceOf[String]
    parsers.parserBank.parserInUse(parserName) match {
      case Nil =>
        Dialog.showMessage(splitPane, "Parser '%s' is not used%n".format(parserName),
            "Parser references", Dialog.Message.Info, null)
      case lst => 
        Dialog.showInput(splitPane, "Select parser", "Parser references",
            Dialog.Message.Question, null, lst, lst(0)) match {
              //case Some(p) => parserChooser.setSelectedItem(p.substring(0, p.indexOf('.')))
              case Some(p) => parserChooser.setSelectedItem(p)
              case None =>
            }
    }
    /* VllGui.top. */cursor = defaultCursor
    }
  }

  val parserMenu = new Menu("Parsers") {
    contents.append(new MenuItem(parserNewAction), new MenuItem(parserRenameAction), 
    new MenuItem(parserReferencesAction), new Separator, new MenuItem(parserDeleteAction))
  }
  
  def selectToken(title: String): Option[String] = {
    def sample(s: String) = if (s.length > 20) (s.substring(0, 20) + "...") else s
    val tokenRE = """(\S+)\s+.+""".r
    if (parsers.tokenBank.isEmpty) {
      Dialog.showMessage(splitPane, "No tokens defined yet", title, Dialog.Message.Info, null)
      None
    } else {
      val tokenNames = parsers.tokenBank.getTokenNames
      val dispList: Array[String] = tokenNames map (n => parsers.tokenBank(n) match {
          case l: Left[String, String] => "%s  (literal: %s )".format(n, sample(l.a))
          case r: Right[String, String] => "%s  (regex: %s )".format(n, sample(r.b))
        }) toArray;
      Dialog.showInput(splitPane, "Select token", title,
          Dialog.Message.Question, null, dispList, dispList(0)) match {
        case Some(s) =>
          s match {
            case tokenRE(t) => Some(t)
          }
        case None => None
      }
    }
  }

  def createNewToken(isRegex: Boolean) {
    val pattern = """\s*([a-zA-Z_]\w*)\s*,\s*(.+?)\s*""".r
    val title = "New " + (if (isRegex) "regex" else "literal")
    val msg = "Enter name, comma, " + (if (isRegex) "regex" else "literal")
    Dialog.showInput(splitPane, msg, title, Dialog.Message.Question, null, Array[String](), null) match {
      case Some(tokenInfo) =>
        tokenInfo match {
          case pattern(name, value) =>
            if (parsers.tokenBank contains name) {
              Dialog.showMessage(splitPane, "A token named '" + name + "' already exists", title, Dialog.Message.Error, null)
            } else if (parsers.tokenBank.valuesIterator.exists(_.equals(if (isRegex) Right(value) else Left(value)))) {
              Dialog.showMessage(splitPane, "Another token with value '" + value + "' exists", title, Dialog.Message.Error, null)
            } else {
              if (isRegex) {
                try {
                  //val rex = new RegExp(Utils.unEscape(value), RegExp.NONE).toAutomaton
                  //if (rex.getShortestExample(true).isEmpty) {
                  if (Automata.canMatchEmptyString(Utils.unEscape(value))) {
                    Dialog.showMessage(splitPane, "'%s' matches empty string, not allowed".format(value), title, Dialog.Message.Error, null)
                  } else {
                    Automata.testRegexp(Utils.unEscape(value))
                    parsers.tokenBank(name) = Right(value)
                  }
                } catch {
                  case x => Dialog.showMessage(splitPane, "Error in '%s': %s".format(value, x.getMessage), title, Dialog.Message.Error, null)
                }
              } else {
                try {
                  Utils.unEscape(value)
                  parsers.tokenBank(name) = Left(value)
                } catch {
                  case x =>
                    Dialog.showMessage(splitPane, x.getMessage, title, Dialog.Message.Error, null)
                }
              }
              isDirty = true
            }
          case _ =>
            Dialog.showMessage(splitPane, "Bad input. Expected name, comma, regex/literal", title, Dialog.Message.Error, null)
        }
      case None =>
    }
  }

  val tokenNewLiteralAction = new Action("New literal") {
    icon = swing.Swing.Icon(getClass.getResource("images/NewLiteral.gif"))
    toolTip = title
    def apply {
      createNewToken(false)
    }
  }

  val tokenNewRegexAction = new Action("New regex") {
    icon = swing.Swing.Icon(getClass.getResource("images/NewRegex.gif"))
    toolTip = title
    def apply {
      createNewToken(true)
    }
  }

  val tokenEditAction = new Action("Edit token") {
    icon = swing.Swing.Icon(getClass.getResource("images/Edit16.gif"))
    toolTip = title
    def apply {
      selectToken("Edit token") match {
        case Some(tokenName) =>
          val tokenVal = parsers.tokenBank(tokenName)
          val (str, title, isRegex) = tokenVal match {
            case Left(literal) => (literal, "Edit literal '%s'".format(tokenName), false)
            case Right(regex) => (regex, "Edit regex '%s'".format(tokenName), true)
          }
          Dialog.showInput(splitPane, title, "Edit token", Dialog.Message.Question, null, Array[String](), str) match {
            case Some(newVal) =>
              parsers.tokenBank(tokenName) = if (isRegex) Right(newVal) else Left(newVal)
            case None =>
          }
        case None =>
      }
    }
  }
  
  val tokenReferencesAction = new Action("Find token") {
    icon = swing.Swing.Icon(getClass.getResource("images/Replace16.gif"))
    toolTip = title
    def apply {
    val defaultCursor = /* VllGui.top. */cursor
    /* VllGui.top. */cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    selectToken("Token references") match {
        case Some(tokenName) =>
          parsers.parserBank.tokenInUse(tokenName) match {
            case Nil => 
              Dialog.showMessage(splitPane, "Token '%s' is not used%n".format(tokenName),
                  "Token references", Dialog.Message.Info, null)
            case lst =>
              Dialog.showInput(splitPane, "Select parser", "Token references",
                  Dialog.Message.Question, null, lst, lst(0)) match {
              //case Some(p) => parserChooser.setSelectedItem(p.substring(0, p.indexOf('.')))
              case Some(p) => parserChooser.setSelectedItem(p)
              case None =>
            }
          }
        case None =>
    }
    /* VllGui.top. */cursor = defaultCursor
    }
  }
  
  val tokenDeleteAction = new Action("Delete token") {
    icon = swing.Swing.Icon(getClass.getResource("images/Delete16.gif"))
    toolTip = title
    def apply {
    selectToken("Delete token") match {
        case Some(tokenName) =>
          parsers.parserBank.tokenInUse(tokenName) match {
            case Nil => parsers.tokenBank.remove(tokenName)
              isDirty = true
            case lst: Seq[String] =>
              val msg = "Can't delete '%s' - used by: \n%s".format(tokenName, lst.mkString(",\n"))
              Dialog.showMessage(splitPane, msg, "Delete token", Dialog.Message.Error, null)
          }
        case None =>
    }
    }
  }

  val tokenImportAction = new Action("Import tokens") {
    icon = swing.Swing.Icon(getClass.getResource("images/Import16.gif"))
    toolTip = title
    def apply {
      tokenLibraryChooser.title = "Import tokens"
      tokenLibraryChooser.showOpenDialog(splitPane) match {
        case FileChooser.Result.Approve =>
          val tokenFile = tokenLibraryChooser.selectedFile
          try {
            parsers.importTokens(tokenFile)
          } catch {
            case e: Exception => 
              Dialog.showMessage(splitPane, "%s(%s)".format(e.getClass.getName, e.getMessage), 
                                 "Import tokens", Dialog.Message.Error, null)
          }
        case _ =>
      }
    }
  }

  val tokenExportAction = new Action("Export tokens") {
    icon = swing.Swing.Icon(getClass.getResource("images/Export16.gif"))
    toolTip = title
    def apply {
        tokenLibraryChooser.title = "Export tokens"
        tokenLibraryChooser.showSaveDialog(splitPane) match {
          case FileChooser.Result.Approve =>
            if (tokenLibraryChooser.selectedFile.exists) {
              val overwrite = Dialog.showConfirmation(splitPane, "File exists. Overwrite?",
                  "Export tokens", Dialog.Options.YesNo, Dialog.Message.Question, null)
              if (overwrite != Dialog.Result.Yes)
                return
            }
            val tokenFile = tokenLibraryChooser.selectedFile
            parsers.exportTokens(tokenFile)
          case _ =>
        }
    }
  }

  val tokensMenu = new Menu("Tokens") {
    contents.append(new MenuItem(tokenNewLiteralAction), new MenuItem(tokenNewRegexAction), 
    new MenuItem(tokenEditAction), new MenuItem(tokenReferencesAction), new Separator,
    new MenuItem(tokenDeleteAction), new Separator, new MenuItem(tokenImportAction), new MenuItem(tokenExportAction))
  }

  val globalCommentAction = new Action("Comment") {
    def apply {
    Dialog.showInput(splitPane, "Comment regex", "Define comment", Dialog.Message.Question, null, Array[String](), parsers.comment) match {
      case Some(comments) =>
        parsers.comment = comments.asInstanceOf[String]
        isDirty = true
      case None =>
    }
    }
  }
  
  val globalWhitespaceAction = new Action("Whitespace") {
    def apply {
    Dialog.showInput(splitPane, "Whitespace regex", "Define whitespace", Dialog.Message.Question, 
        null, Array[String](), Utils.reEscape(parsers.wspace)) match {
      case Some(whiteSpace) =>
        parsers.wspace = whiteSpace.asInstanceOf[String]
        isDirty = true
      case None =>
    }
    }
  }
  
  val globalsMenu = new Menu("Globals") {
    contents.append(new MenuItem(globalWhitespaceAction), new MenuItem(globalCommentAction))/*  +
    globalFlattenTilde + globalJavaizeLists */
  }
  
  val runParseInputAction = new Action("Parse input") {
    toolTip = title
    icon = swing.Swing.Icon(getClass.getResource("images/AlignLeft16.gif"))
    def apply { parsers ! null; /* println("parsers ! null") */ }
  }
  
  val runParseFileAction = new Action("Parse file") {
    toolTip = title
    icon = swing.Swing.Icon(getClass.getResource("images/Host16.gif"))
    def apply {
      inFileChooser.showOpenDialog(splitPane) match {
        case FileChooser.Result.Approve =>
          val inFile = inFileChooser.selectedFile
          parsers ! inFile
        case _ =>
      }
    }
  }
  
   val runTraceAllMenuItem = new CheckMenuItem("Trace all") {
    reactions += {case ButtonClicked(_) => parsers.traceAll = selected}
  }
  
  val runHandlerBasicMenuItem = new RadioMenuItem("Basic")
  val runHandlerStructuredMenuItem = new RadioMenuItem("Structured")
  val customHandlerMenuItem = new RadioMenuItem("Custom") {
    reactions += {case ButtonClicked(_) =>
         val oldClassName = customTreeHandlerClassName match {
          case Some(cn) => cn
          case None => null
        }
        Dialog.showInput(splitPane, "Enter class-name", "Custom tree handler",
                         Dialog.Message.Question, null, Array[String](), oldClassName) match {
          case Some(className) =>
            try {
              customTreeHandler = Some(Class.forName(className).newInstance.asInstanceOf[ParseTreeHandler])
              customTreeHandlerClassName = Some(className)
            } catch {
              case _ =>
                val url = getClass.getResource("images/Icon.gif").toString
                val ext = "jar:(.+?/)[^/!]+!/.+".r
                url match {case ext(u) =>
                  val clUrl = new URL(u)
                  val cl = new URLClassLoader(Array[URL](clUrl))
                  try {
                    customTreeHandler = Some(cl.loadClass(className).newInstance.asInstanceOf[ParseTreeHandler])
                    customTreeHandlerClassName = Some(className)
                  } catch {
                    case ex =>
                      Dialog.showMessage(splitPane, ex, "Custom tree handler", Dialog.Message.Error, null)
                      runHandlerBasicMenuItem.selected = true
                  }
                }
            }
          case _ => runHandlerBasicMenuItem.selected = true
        }
    } 
  }
  val hdlrBtnGrp = new ButtonGroup(runHandlerBasicMenuItem, runHandlerStructuredMenuItem, customHandlerMenuItem)
  runHandlerBasicMenuItem.selected = true
  val treeHandlerMenu = new Menu("Tree handler") {
    contents.append(runHandlerBasicMenuItem, runHandlerStructuredMenuItem, customHandlerMenuItem)
  }
  val testMenu = new Menu("Test") {
    contents.append(new MenuItem(runParseInputAction), new MenuItem(runParseFileAction), new Separator,
    treeHandlerMenu, runTraceAllMenuItem)
  }

  val runClearLogAction = new Action("Clear log") {
    toolTip = title
    icon = swing.Swing.Icon(getClass.getResource("images/Clear16.gif"))
    def apply {
      logTextPane.clearLogText()
      //parsers.textPane.clearLogText()
    }
  }

  val runCopyLogAction = new Action("Copy log") {
    toolTip = title
    icon = swing.Swing.Icon(getClass.getResource("images/Copy16.gif"))
    def apply {
      logTextPane.copyLog()
      //textPane.copyLogText()
    }
  }

  val logMenu = new Menu("Log") {
    contents.append(new MenuItem(runCopyLogAction), new MenuItem(runClearLogAction))
  }

  val aboutMenuItem = new MenuItem("About VisualLangLab") {
    reactions += {case ButtonClicked(_) =>
        new AboutHelper(mainFrame).presentAboutDialog()
    }
  }
  val helpSampleItem1 = new MenuItem("ArithExpr") {
    reactions += {
      case ButtonClicked(_) => new ArithExpr(mainFrame).load()
    }
  }
  val helpSampleItem2 = new MenuItem("SimpleJSON") {
    reactions += {
      case ButtonClicked(_) => new SimpleJSON(mainFrame).load()
    }
  }
  val helpSampleItem3 = new MenuItem("ArithExpr with action-code") {
    reactions += {
      case ButtonClicked(_) => new ArithExprWithActionCode(mainFrame).load()
    }
  }
  val helpSamples = new Menu("Sample grammars") {
    contents.append(helpSampleItem1, helpSampleItem2, helpSampleItem3)
  }
  val helpMenuItem = new MenuItem("Help") {
    val msg = "Not yet implemented\nCheck for documentation at https://vll.dev.java.net"
    reactions += {case ButtonClicked(_) =>
        Dialog.showMessage(splitPane, msg, "VisualLangLab", Dialog.Message.Info, null)
    }
  }
  val helpMenu = new Menu("Help") {
    contents.append(helpMenuItem, new Separator, helpSamples, new Separator, aboutMenuItem)
  }

  menuBar = new MenuBar {
    contents.append(fileMenu, viewMenu, tokensMenu, parserMenu, globalsMenu, testMenu, logMenu, helpMenu)
  }
  
  val backButton: Button = new Button() {
    icon = swing.Swing.Icon(getClass.getResource("images/Back16.gif"))
    tooltip = "Back"
    enabled = false
    reactions += {case bc: ButtonClicked =>
         if (!parserTreePanel.displayStack.isEmpty) {
          val parserName = parserTreePanel.displayStack.pop
          //println("popped: " + parserName)
          lastPoppedName = parserName
          parserChooser.setSelectedItem(parserName)
          if (parserTreePanel.displayStack.isEmpty)
            enabled = false
        }
     }
  }
  
  def actionPerformed(ae: ActionEvent) {
    ae.getSource match {
      case `parserChooser` =>
        val pName = parserChooser.getSelectedItem.asInstanceOf[String]
//        if (pName != lastPoppedName) {
//          ParserTreePanel.displayStack.push(ParserTreePanel.rootNode.nodeName)
//          LeftContainerPanel.backButton.enabled = true
//        }
        parserTreePanel.setParser(pName)
    }
  }
  
  def updateParserChooser(selectedName: String) {
    parserChooser.removeActionListener(this)
    parserChooser.removeAllItems
    val parserNames = parsers.parserBank.parserNames
    parserNames.foreach(r => parserChooser.addItem(r))
    parserChooser.setMaximumSize(parserChooser.getPreferredSize)
    parserChooser.setSelectedItem(selectedName)
    parserChooser.addActionListener(this)
  }

  private val tk = java.awt.Toolkit.getDefaultToolkit
  val screenWidth = tk.getScreenSize.width
  val screenHeight = tk.getScreenSize.height
  val frameWidth = (screenWidth * 0.7).asInstanceOf[Int]
  val treeWidth = (frameWidth * 0.25).asInstanceOf[Int]
  val frameHeight = (screenHeight * 0.75).asInstanceOf[Int]
  location = new Point((screenWidth - frameWidth) / 2, (screenHeight - frameHeight) / 2)
  size = new Dimension(frameWidth, frameHeight)
  preferredSize = new Dimension(frameWidth, frameHeight)

  var lastPoppedName = ""
  val parserChooser = new JComboBox()
  parserChooser.addActionListener(this)
  var isDirty = false
  val parsers = new ParsingActor(this)
  
  val stopAction = new Action("") {
    icon = swing.Swing.Icon(getClass.getResource("images/Stop16.gif"))
    toolTip = "Stop parsing"
    enabled = false
    def apply() {parsers.userRequestedStop = true}
  }

  val stopButton = new Button(stopAction) 
  var currentParserName = "Main"
  var customTreeHandlerClassName: Option[String] = None
  var customTreeHandler: Option[ParseTreeHandler] = None
  val parserTreePanel = new ParserTreePanel(this, parsers)
  private var grammarFile: Option[File] = None
  val logTextPane = new LogTextPane(this)
  val typeDisplayPane = new ParseTreeUtility(this/* , logTextPane */)
  //val innerSplitPane = new SplitPane(Orientation.Vertical, typeDisplayPane, logTextPane) {

  //}
  //val leftSplitPane = new SplitPane(Orientation.Horizontal, parserTreePanel, typeDisplayPane)
  val splitPane = new SplitPane(Orientation.Vertical, parserTreePanel, typeDisplayPane) {
    dividerLocation = treeWidth
  }

  val outerSplitPane = new SplitPane(Orientation.Horizontal, splitPane, logTextPane) {
//    dividerLocation = treeWidth
  }

  val vllIconImage = swing.Swing.Icon(getClass.getResource("images/Icon.gif"))
  iconImage = vllIconImage.getImage
  title = "VisualLangLab/S"
  val grammarFileChooser = new FileChooser(new File(System.getProperty("user.dir"))) {
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false
    fileFilter = new FileFilter() {
      def accept(f: File) = (f.getName endsWith ".vll") || f.isDirectory
      def getDescription = "VisualLangLab grammar (vll) file"
    }
  }
  val tokenLibraryChooser = new FileChooser(new File(System.getProperty("user.dir"))) {
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false
    fileFilter = new FileFilter() {
      def accept(f: File) = (f.getName endsWith ".vll") || f.isDirectory
      def getDescription = "Token library (vll) file"
    }
  }

  val inFileChooser = new FileChooser(new File(System.getProperty("user.dir"))) {
    fileSelectionMode = FileChooser.SelectionMode.FilesAndDirectories
    multiSelectionEnabled = false
    fileFilter = new FileFilter() {
      def accept(f: File) = true
      def getDescription = "Source file"
    }
  }

  val toolBar = new JToolBar()
  toolBar.setFloatable(false)
  toolBar.add(fileNewAction.peer)
  toolBar.add(fileOpenAction.peer)
  toolBar.add(fileSaveAction.peer)
  toolBar.add(fileSaveAsAction.peer)
  toolBar.addSeparator()
  toolBar.add(backButton.peer)
  toolBar.add(parserChooser)
  toolBar.addSeparator()
  toolBar.add(parserNewAction.peer)
  toolBar.add(parserReferencesAction.peer)
  toolBar.add(parserRenameAction.peer)
  toolBar.addSeparator()
  toolBar.add(tokenNewLiteralAction.peer)
  toolBar.add(tokenNewRegexAction.peer)
  toolBar.add(tokenReferencesAction.peer)
  toolBar.add(tokenEditAction.peer)
  toolBar.add(tokenImportAction.peer)
  toolBar.add(tokenExportAction.peer)
  toolBar.addSeparator()
  toolBar.add(runParseInputAction.peer)
  toolBar.add(runParseFileAction.peer)
  toolBar.add(stopButton.peer)
  toolBar.addSeparator()
  toolBar.add(runCopyLogAction.peer)
  toolBar.add(runClearLogAction.peer)
   
  contents = new BorderPanel {
    add(outerSplitPane, BorderPanel.Position.Center)
    add(Component.wrap(toolBar), BorderPanel.Position.North)
  }

  updateParserChooser("Main")
  private val mainFrame = this

  System.setOut(new PrintStream(new OutStream(logTextPane, false)))
  System.setErr(new PrintStream(new OutStream(logTextPane, true)))
}

object VllGui extends SimpleSwingApplication {
  ScalaEngine.compile("(x:Any) => x")
  var top = new VllGui()
}
