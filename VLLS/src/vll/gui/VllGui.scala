/*
 Copyright 2010, Sanjay Dasgupta
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

package vll.gui

//import dk.brics.automaton.RegExp
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Cursor
import java.awt.Dimension
import scala.swing.Dialog
import scala.swing.FileChooser
import java.awt.Point
import java.awt.SplashScreen
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
import scala.swing.Swing
import scala.swing.event.ButtonClicked
import vll.core.RootNode
import scala.util.Properties
import vll.core.Automata
import vll.core.ScalaEngine
import vll.core.Utils
import vll.gui.samples.{ArithExpr, ArithExprWithActionCode, SimpleJSON}

class VllGui extends MainFrame with ActionListener {
//  VllGui.splash("0")

  try {
    if (Properties.osName.matches(".*[wW]indows.*"))
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
  } catch  {
    case e: Throwable => println(e)
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
    icon = Images.new16 
    toolTip = title
    def apply {
      saveChanges()
      parsers.reset()
      if (!isDirty) {
        GuiNode.clearCache()
        ruleTreePanel.setRule("Main")
        updateRuleChooser("Main")
        grammarFile = None
        mainFrame.title = "VisualLangLab/S"
      }
      customTreeHandlerClassName = None
      customTreeHandler = None
      runHandlerBasicMenuItem.selected = true
    }
  }
  
  val fileOpenAction = new Action("Open") {
    icon = Images.open16 //swing.Swing.Icon(getClass.getResource("images/Open16.gif"))
    toolTip = title
    def apply {
      saveChanges()
      parsers.reset()
      if (!isDirty) {
        GuiNode.clearCache()
        grammarFileChooser.title = "Open"
        grammarFileChooser.showOpenDialog(splitPane) match {
          case FileChooser.Result.Approve =>
            grammarFile = Some(grammarFileChooser.selectedFile)
            try {
              parsers.load(grammarFile.get)
            } catch {
              case e: IOException => val sb = new StringBuilder(); sb.append("Missing ")
                  Dialog.showMessage(splitPane, sb.toString, "ERROR - File open", Dialog.Message.Error, null)
            }
            val firstRule = parsers.ruleBank.ruleNames(0)
            updateRuleChooser(firstRule)
            ruleTreePanel.setRule(firstRule)
            mainFrame.title = "VisualLangLab/S - " + grammarFile.get.getName
          case _ =>
        }
      }
    }
  }
  
  val fileSaveAction = new Action("Save") {
    icon = Images.save16 //swing.Swing.Icon(getClass.getResource("images/Save16.gif"))
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
    icon = Images.saveAs16 //swing.Swing.Icon(getClass.getResource("images/SaveAs16.gif"))
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
        ruleTreePanel.peer.repaint()
        ruleTreePanel.theModel.nodeStructureChanged(ruleTreePanel.rootNode)
        for (i <- 0 until 20)
          ruleTreePanel.theTree.expandRow(i)
   }
  }

  val viewShowEpsOkMenuItem: CheckMenuItem = new CheckMenuItem("Show \u03b5-ok items") {
    reactions += {case ButtonClicked(_) => 
        ruleTreePanel.peer.repaint()
        ruleTreePanel.theModel.nodeStructureChanged(ruleTreePanel.rootNode)
        for (i <- 0 until 20)
          ruleTreePanel.theTree.expandRow(i)
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
  
  val ruleNewAction = new Action("New rule") {
    icon = Images.newReference //swing.Swing.Icon(getClass.getResource("images/NewReference.gif"))
    toolTip = title
    def apply {
    val pattern = """\s*([a-zA-Z_]\w*)\s*""".r
    Dialog.showInput(splitPane, "Enter rule-name", "New rule", Dialog.Message.Question, null, Array[String](), null) match {
      case Some(name) =>
        name match {
          case pattern(newRuleName) =>
            if (parsers.ruleBank contains newRuleName) {
              Dialog.showMessage(splitPane, "A rule named '" + newRuleName + "' already exists", "ERROR - New rule", Dialog.Message.Error, null)
            } else {
              val newRoot = /* new  */RootNode(newRuleName)
              parsers.ruleBank(newRuleName) = newRoot
              ruleTreePanel.setRule(newRuleName)
              updateRuleChooser(newRuleName)
              isDirty = true
              currentRuleName = newRuleName
            }
          case _ =>
            Dialog.showMessage(splitPane, "Bad rule name - identifier expected", "ERROR - New rule", Dialog.Message.Error, null)
        }
      case None =>
    }
    }
  }
  
  val ruleRenameAction = new Action("Rename rule") {
    icon = Images.refresh16 //swing.Swing.Icon(getClass.getResource("images/Refresh16.gif"))
    toolTip = title
    def apply {
    val currentName = ruleChooser.getSelectedItem.asInstanceOf[String]
    val pattern = """\s*([a-zA-Z_]\w*)\s*""".r
    Dialog.showInput(splitPane, "Enter new name", "Rename rule", Dialog.Message.Question, null, Array[String](), currentName) match {
      case Some(name) =>
        name match {
          case pattern(newRuleName) =>
            if (parsers.ruleBank contains newRuleName) {
              Dialog.showMessage(splitPane, "A rule named '" + newRuleName + "' already exists", "ERROR - Rename rule", Dialog.Message.Error, null)
            } else {
              parsers.ruleBank.rename(currentName, newRuleName)
              ruleTreePanel.setRule(newRuleName)
              updateRuleChooser(newRuleName)
              isDirty = true
              currentRuleName = newRuleName
            }
          case _ =>
            Dialog.showMessage(splitPane, "Bad rule name - identifier expected", "ERROR - Rename rule", Dialog.Message.Error, null)
        }
      case None =>
    }
    }
  }
  
  val ruleDeleteAction = new Action("Delete rule") {
    toolTip = title
    icon = Images.delete16 //swing.Swing.Icon(getClass.getResource("images/Delete16.gif"))
    def apply {
      def purgeStack(p: String) {
        var lst = List[String]()
        while(!ruleTreePanel.displayStack.isEmpty)
          lst ::= ruleTreePanel.displayStack.pop
        lst.filter(_ != p).foreach(ruleTreePanel.displayStack.push)
      }
      val ruleToDelete = ruleChooser.getSelectedItem.asInstanceOf[String]
      val ok = Dialog.showConfirmation(splitPane, "Delete '" + ruleToDelete + "' ?",
          "Delete rule", Dialog.Options.YesNo, Dialog.Message.Question, null)
      if (ok == Dialog.Result.Yes) {
        val ruleNames = parsers.ruleBank.ruleNames 
        if (ruleNames.size == 1) {
          Dialog.showMessage(splitPane, "Can't delete last rule (use rename instead)",
              "ERROR - Delete rule", Dialog.Message.Error, null)
        } else {
          parsers.ruleBank.ruleInUse(ruleToDelete, true) match {
            case Nil =>
              val idx = ruleNames.indexOf(ruleToDelete)
                parsers.ruleBank -= ruleToDelete
                val newDisplay = if (idx == ruleNames.size - 1)
                  ruleNames(idx - 1) else ruleNames(idx + 1)
                updateRuleChooser(newDisplay)
                ruleTreePanel.setRule(newDisplay)
                purgeStack(ruleToDelete)
                isDirty = true
            case users: Seq[String] =>
                val msg = "Can't delete '%s' - used by: \n%s".format(ruleToDelete, users.mkString(",\n"))
                Dialog.showMessage(splitPane, msg, "ERROR - Delete rule", Dialog.Message.Error, null)
          }
        }
      }
    }
  }
  
  val ruleReferencesAction = new Action("Find rule") {
    toolTip = title
    icon = Images.search16 //swing.Swing.Icon(getClass.getResource("images/Search16.gif"))
    def apply {
    val defaultCursor = /* VllGui.top. */cursor
    cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    val ruleName = ruleChooser.getSelectedItem.asInstanceOf[String]
    parsers.ruleBank.ruleInUse(ruleName) match {
      case Nil =>
        Dialog.showMessage(splitPane, "Rule '%s' is not used%n".format(ruleName),
            "Rule references", Dialog.Message.Info, null)
      case lst => 
        Dialog.showInput(splitPane, "Select rule", "Rule references",
            Dialog.Message.Question, null, lst, lst(0)) match {
              case Some(p) => ruleChooser.setSelectedItem(p)
              case None =>
            }
    }
    cursor = defaultCursor
    }
  }

  val ruleMenu = new Menu("Rules") {
    contents.append(new MenuItem(ruleNewAction), new MenuItem(ruleRenameAction), 
    new MenuItem(ruleReferencesAction), new Separator, new MenuItem(ruleDeleteAction))
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
  
  def validateAndAssignTokenValue(isNew: Boolean, isRegex: Boolean, name: String, value: String) {
    val errorTitle = "ERROR - " + (if (isNew) "New " else "Edit ") + (if (isRegex) "regex" else "literal")
    if (isNew && (parsers.tokenBank contains name)) {
      Dialog.showMessage(splitPane, "A token named '" + name + "' already exists", errorTitle, Dialog.Message.Error, null)
    } else if (parsers.tokenBank.exists(p => {(p._1.endsWith("_") == name.endsWith("_")) &&
        p._2.equals(if (isRegex) Right(value) else Left(value))})) {
      val otherToken = parsers.tokenBank.find(p => {(p._1.endsWith("_") == name.endsWith("_")) &&
           p._2.equals(if (isRegex) Right(value) else Left(value))}).get._1
      Dialog.showMessage(splitPane, "Token '%s' has same value, not allowed".format(otherToken), errorTitle, Dialog.Message.Error, null)
    } else {
      if (isRegex) {
         try {
           if (Automata.canMatchEmptyString(Utils.unEscape(value))) {
             Dialog.showMessage(splitPane, "'%s' matches empty string, not allowed".format(value), errorTitle, Dialog.Message.Error, null)
           } else {
             Automata.testRegexp(Utils.unEscape(value))
             parsers.tokenBank(name) = Right(value)
           }
         } catch {
           case x => Dialog.showMessage(splitPane, "Error in '%s': %s".format(value, x.getMessage), errorTitle, Dialog.Message.Error, null)
         }
       } else {
         try {
           Utils.unEscape(value)
           parsers.tokenBank(name) = Left(value)
         } catch {
            case x =>
              Dialog.showMessage(splitPane, x.getMessage, errorTitle, Dialog.Message.Error, null)
         }
       }
       isDirty = true
     }
  }

  def createNewToken(isRegex: Boolean) {
    val pattern = if (isRegex)
      """([a-zA-Z_][a-zA-Z_0-9]*(?:~\d+)?)(\s*,\s*|\s+)(\S.*)""".r
    else
      """([a-zA-Z_][a-zA-Z_0-9]*)(\s*,\s*|\s+)(\S.*)""".r
    val inputDescription = "name, space(s), %s-pattern".format(if (isRegex) "regex" else "literal")
    val msg = "Enter " + inputDescription
    Dialog.showInput(splitPane, msg, "New " + (if (isRegex) "regex" else "literal"), Dialog.Message.Question, null, Array[String](), null) match {
      case Some(tokenInfo) =>
        tokenInfo.trim match {
          case pattern(name, sep, value) => 
            if (sep.trim.length == 0)
              validateAndAssignTokenValue(true, isRegex, name, value)
            else
              Dialog.showMessage(splitPane, "Bad input. Need: " + inputDescription +  
                  ".\nFrom Version-7.01 a comma separator is not accepted",
                  "ERROR - New " + (if (isRegex) "regex" else "literal"), Dialog.Message.Error, null)
          case _ =>
            Dialog.showMessage(splitPane, "Bad input. Need: " + inputDescription + ".",
                "ERROR - New " + (if (isRegex) "regex" else "literal"), Dialog.Message.Error, null)
        }
      case None =>
    }
  }

  val tokenNewLiteralAction = new Action("New literal") {
    icon = Images.newLiteral //swing.Swing.Icon(getClass.getResource("images/NewLiteral.gif"))
    toolTip = title
    def apply {
      createNewToken(false)
    }
  }

  val tokenNewRegexAction = new Action("New regex") {
    icon = Images.newRegex //swing.Swing.Icon(getClass.getResource("images/NewRegex.gif"))
    toolTip = title
    def apply {
      createNewToken(true)
    }
  }

  val tokenEditAction = new Action("Edit token") {
    icon = Images.edit16 //swing.Swing.Icon(getClass.getResource("images/Edit16.gif"))
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
              if (newVal != str) validateAndAssignTokenValue(false, isRegex, tokenName, newVal)
            case None =>
          }
        case None =>
      }
    }
  }
  
  val tokenReferencesAction = new Action("Find token") {
    icon = Images.replace16 //swing.Swing.Icon(getClass.getResource("images/Replace16.gif"))
    toolTip = title
    def apply {
    val defaultCursor = cursor
    cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    selectToken("Token references") match {
        case Some(tokenName) =>
          parsers.ruleBank.tokenInUse(tokenName) match {
            case Nil => 
              Dialog.showMessage(splitPane, "Token '%s' is not used%n".format(tokenName),
                  "Token references", Dialog.Message.Info, null)
            case lst =>
              Dialog.showInput(splitPane, "Select rule", "Token references",
                  Dialog.Message.Question, null, lst, lst(0)) match {
              case Some(p) => ruleChooser.setSelectedItem(p)
              case None =>
            }
          }
        case None =>
    }
    cursor = defaultCursor
    }
  }
  
  val tokenDeleteAction = new Action("Delete token") {
    icon = Images.delete16 //swing.Swing.Icon(getClass.getResource("images/Delete16.gif"))
    toolTip = title
    def apply {
    selectToken("Delete token") match {
        case Some(tokenName) =>
          parsers.ruleBank.tokenInUse(tokenName) match {
            case Nil => parsers.tokenBank.remove(tokenName)
              isDirty = true
            case lst: Seq[String] =>
              val msg = "Can't delete '%s' - used by: \n%s".format(tokenName, lst.mkString(",\n"))
              Dialog.showMessage(splitPane, msg, "ERROR - Delete token", Dialog.Message.Error, null)
          }
        case None =>
    }
    }
  }

  val tokenImportAction = new Action("Import tokens") {
    icon = Images.import16 //swing.Swing.Icon(getClass.getResource("images/Import16.gif"))
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
                                 "ERROR - Import tokens", Dialog.Message.Error, null)
          }
        case _ =>
      }
    }
  }

  val tokenExportAction = new Action("Export tokens") {
    icon = Images.export16 //swing.Swing.Icon(getClass.getResource("images/Export16.gif"))
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
        null, Array[String](), parsers.wspace) match {
      case Some(whiteSpace) =>
        parsers.wspace = whiteSpace.asInstanceOf[String]
        isDirty = true
      case None =>
    }
    }
  }
  
  val globalsMenu = new Menu("Globals") {
    contents.append(new MenuItem(globalWhitespaceAction), new MenuItem(globalCommentAction))
  }
  
  val runParseInputAction = new Action("Parse input") {
    toolTip = title
    icon = Images.alignLeft16 //swing.Swing.Icon(getClass.getResource("images/AlignLeft16.gif"))
    def apply { parsers ! null; }
  }
  
  val runParseFileAction = new Action("Parse file") {
    toolTip = title
    icon = Images.host16 //swing.Swing.Icon(getClass.getResource("images/Host16.gif"))
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
                      Dialog.showMessage(splitPane, ex, "ERROR - Custom tree handler", Dialog.Message.Error, null)
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
    icon = Images.clear16 //swing.Swing.Icon(getClass.getResource("images/Clear16.gif"))
    def apply {
      logTextPane.clearLogText()
    }
  }

  val runCopyLogAction = new Action("Copy log") {
    toolTip = title
    icon = Images.copy16 //swing.Swing.Icon(getClass.getResource("images/Copy16.gif"))
    def apply {
      logTextPane.copyLog()
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
  val scalaLicenseMenuItem = new MenuItem("Scala license") {
    reactions += {case ButtonClicked(_) =>
        new ScalaLicenseHelper(mainFrame).presentAboutDialog()
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
    val msg = "Check for documentation at http://vll.java.net"
    reactions += {case ButtonClicked(_) =>
        Dialog.showMessage(splitPane, msg, "VisualLangLab", Dialog.Message.Info, null)
    }
  }
  val helpMenu = new Menu("Help") {
    contents.append(helpMenuItem, new Separator, helpSamples, new Separator, aboutMenuItem, 
                    scalaLicenseMenuItem)
  }

  menuBar = new MenuBar {
    contents.append(fileMenu, viewMenu, tokensMenu, ruleMenu, globalsMenu, testMenu, logMenu, helpMenu)
  }
  
  val backButton: Button = new Button() {
    icon = swing.Swing.Icon(getClass.getResource("images/Back16.gif"))
    tooltip = "Back"
    enabled = false
    reactions += {case bc: ButtonClicked =>
        if (ruleTreePanel.displayStack.size > 1) {
          ruleTreePanel.displayStack.pop
          val ruleName = ruleTreePanel.displayStack(0)
          lastPoppedName = ruleName
          ruleChooser.setSelectedItem(ruleName)
        }
        enabled = ruleTreePanel.displayStack.size > 1
     }
  }
  
  def actionPerformed(ae: ActionEvent) {
    ae.getSource match {
      case `ruleChooser` =>
        val pName = ruleChooser.getSelectedItem.asInstanceOf[String]
        ruleTreePanel.setRule(pName)
    }
  }
  
  def updateRuleChooser(selectedName: String) {
    ruleChooser.removeActionListener(this)
    ruleChooser.removeAllItems
    val ruleNames = parsers.ruleBank.ruleNames
    ruleNames.foreach(r => ruleChooser.addItem(r))
    ruleChooser.setMaximumSize(ruleChooser.getPreferredSize)
    ruleChooser.setSelectedItem(selectedName)
    ruleChooser.addActionListener(this)
  }
  
  def setWaitingCursor(waiting: Boolean) {
    cursor = if (waiting) Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR) else defaultCursor
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
//  val ruleChooser = new JComboBox() // JDK 1.6
  val ruleChooser = new JComboBox[String]() // JDK 1.7
  ruleChooser.addActionListener(this)
  var isDirty = false
  val parsers = new ParsingActor(this)
  
  val stopAction = new Action("") {
    icon = Images.stop16 //swing.Swing.Icon(getClass.getResource("images/Stop16.gif"))
    toolTip = "Stop parsing"
    enabled = false
    def apply() {parsers.userRequestedStop = true}
  }

  private val defaultCursor = cursor
  val stopButton = new Button(stopAction) 
  var currentRuleName = "Main"
  var customTreeHandlerClassName: Option[String] = None
  var customTreeHandler: Option[ParseTreeHandler] = None
  val ruleTreePanel = new RuleTreePanel(this, parsers)
  private var grammarFile: Option[File] = None
  val logTextPane = new LogTextPane(this)
  val typeDisplayPane = new RuleTreeUtility(this/* , logTextPane */)
  val splitPane = new SplitPane(Orientation.Vertical, ruleTreePanel, typeDisplayPane) {
    dividerLocation = treeWidth
  }

  val outerSplitPane = new SplitPane(Orientation.Horizontal, splitPane, logTextPane) {
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
  toolBar.add(ruleChooser)
  toolBar.addSeparator()
  toolBar.add(ruleNewAction.peer)
  toolBar.add(ruleReferencesAction.peer)
  toolBar.add(ruleRenameAction.peer)
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

  updateRuleChooser("Main")
  private val mainFrame = this

  System.setOut(new PrintStream(new OutStream(logTextPane, false)))
  System.setErr(new PrintStream(new OutStream(logTextPane, true)))
}

object VllGui extends SimpleSwingApplication {
  def splash(msg: String) {
      try {
        val t0 = System.currentTimeMillis
        val splashScreen = SplashScreen.getSplashScreen
        val width = splashScreen.getSize.width 
        val height = splashScreen.getSize.height
        val gc = splashScreen.createGraphics
        gc.setComposite(AlphaComposite.Clear)
        gc.setPaintMode()
        gc.setColor(Color.red)
        gc.drawLine(0, 0, width, height)
        gc.drawLine(0, height, width, 0)
        gc.fillRect(10, height - 30, width - 20, 10)
//        Dialog.showMessage(null, "%d (%d,%d) %s".format(System.currentTimeMillis - t0, width, height, gc), "DONE", Dialog.Message.Info, null)
      } catch {
        case e: Exception => Dialog.showMessage(null, e.getMessage, "FAIL", Dialog.Message.Info, null)
      }
  }
  // Required to prevent: java.lang.IllegalArgumentException: Comparison method violates its general contract!
  System.setProperty("java.util.Arrays.useLegacyMergeSort", "true")
  var top: VllGui = null
  Swing.onEDT {top = new VllGui()}
  ScalaEngine.compile("(x: Any) => x")
}
