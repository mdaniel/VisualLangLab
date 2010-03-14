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

package net.java.dev.vll

import scala.swing.{Orientation, Separator, SplitPane, Dialog, FileChooser, BorderPanel, RadioMenuItem, ButtonGroup, CheckMenuItem}
import swing.{MainFrame, Menu, MenuBar, SimpleSwingApplication, MenuItem}
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import java.io.PrintStream
import javax.swing.filechooser.FileFilter
import javax.swing.{UIManager, JComboBox, SwingUtilities}
import scala.swing.event.{ButtonClicked, WindowClosing}
import scala.swing.Swing._
import net.java.dev.vll.samples._

object VisualLangLab extends SimpleSwingApplication with ActionListener {

  try {
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
  } catch  {
    case e: Throwable => System.err.println(e)
  }

  def manageLookNFeel() {
    val currentLaf = UIManager.getLookAndFeel()
    val lInfo = UIManager.getInstalledLookAndFeels();
    val lafNames = new Array[String](lInfo.length)
    for (i <- 0 until lInfo.length) lafNames(i) = lInfo(i).getClassName()
    Dialog.showInput(VisualLangLab.splitPane, "Choose L&F", "Look & Feel", Dialog.Message.Question, null, lafNames, currentLaf.getClass.getName) match {
      case Some(chosenLaf) =>
        try {
          UIManager.setLookAndFeel(chosenLaf)
          SwingUtilities.updateComponentTreeUI(top.peer);
        } catch {case e: Throwable => System.err.println(e)}

      case _ =>
    }
  }

  def saveChanges() {
    if (isDirty) {
      val ok = Dialog.showConfirmation(splitPane, "Save changes?", "Unsaved changes exist",
          Dialog.Options.YesNoCancel, Dialog.Message.Question, null)
      ok match {
        case Dialog.Result.Yes =>
          grammarFile match {
            case Some(file) => FileIO.save(file)
            case None => fileChooser.showSaveDialog(splitPane) match {
                case FileChooser.Result.Approve =>
                  grammarFile = Some(fileChooser.selectedFile)
                  FileIO.save(grammarFile.get)
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

  def editToken() {
    selectToken("Edit token") match {
        case Some(tokenName) =>
          val tokenVal = TokenBank(tokenName)
          val (str, title, isRegex) = tokenVal match {
            case Left(literal) => (literal, "Edit literal value", false)
            case Right(regex) => (regex, "Edit regex value", true)
          }
          Dialog.showInput(VisualLangLab.splitPane, title, "Edit token", Dialog.Message.Question, null, Array[String](), str) match {
            case Some(newVal) =>
              TokenBank(tokenName) = if (isRegex) Right(newVal) else Left(newVal)
            case None =>
          }
        case None =>
    }
  }

  def createNewToken(isRegex: Boolean) {
    val pattern = """\s*([a-zA-Z_]\w*)\s*,\s*(.+?)\s*""".r
    val title = "Create " + (if (isRegex) "regex token" else "literal token")
    val msg = "Enter token-name, " + (if (isRegex) "regex-pattern" else "literal-string")
    Dialog.showInput(VisualLangLab.splitPane, msg, title, Dialog.Message.Question, null, Array[String](), null) match {
      case Some(tokenInfo) =>
        tokenInfo match {
          case pattern(name, value) =>
            if (TokenBank contains name) {
              Dialog.showMessage(splitPane, "A token named '" + name + "' already exists", "Create token", Dialog.Message.Error, null)
            } else {
              TokenBank(name) = if (isRegex) Right(value) else Left(value)
              isDirty = true
            }
          case _ =>
            Dialog.showMessage(splitPane, "Bad input. Enter <name> , <pattern>", "Create token", Dialog.Message.Error, null)
        }
      case None =>
    }
  }

  def selectToken(title: String): Option[String] = {
    val tokenRE = """(\S+)\s+.+""".r
    if (TokenBank.isEmpty) {
      Dialog.showMessage(VisualLangLab.splitPane, "No tokens defined yet", title, Dialog.Message.Info, null)
      None
    } else {
      val tokenNames = TokenBank.getTokenNames
      val dispList: Array[String] = tokenNames map (n => TokenBank(n) match {
          case l: Left[String, String] => "%s  (literal: %s )".format(n, l.a)
          case r: Right[String, String] => "%s  (regex: %s )".format(n, r.b)
        }) toArray;
      Dialog.showInput(VisualLangLab.splitPane, "Select token", title,
          Dialog.Message.Question, null, dispList, dispList(0)) match {
        case Some(s) =>
          s match {
            case tokenRE(t) => Some(t)
          }
        case None => None
      }
    }
  }

  def deleteToken() {
    selectToken("Delete token") match {
        case Some(tokenName) =>
          ParserBank.tokenInUse(tokenName) match {
            case Nil => TokenBank.deleteToken(tokenName)
              isDirty = true
            case lst: List[String] =>
              val msg = "Can't delete '%s' - used by: \n%s".format(tokenName, lst.mkString(",\n"))
              Dialog.showMessage(splitPane, msg, "Delete token", Dialog.Message.Error, null)
          }
        case None =>
    }
  }

  def createNewParser() {
    val pattern = """\s*([a-zA-Z_]\w*)\s*""".r
    Dialog.showInput(VisualLangLab.splitPane, "Enter parser-name", "New parser", Dialog.Message.Question, null, Array[String](), null) match {
      case Some(name) =>
        name match {
          case pattern(newParserName) =>
            if (ParserBank contains newParserName) {
              Dialog.showMessage(splitPane, "A parser named '" + newParserName + "' already exists", "New parser", Dialog.Message.Error, null)
            } else {
              val newRoot = new RootNode(newParserName)
              ParserTreePanel.setParser(newRoot)
              ParserBank(newParserName) = newRoot
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

  def renameParser() {
      Dialog.showMessage(VisualLangLab.splitPane, "Not yet implemented", "Rename parser", Dialog.Message.Info, null)
  }

  def deleteParser() {
    val parser = parserChooser.getSelectedItem.asInstanceOf[String]
    val ok = Dialog.showConfirmation(splitPane, "Delete '" + parser + "' ?",
        "Delete parser", Dialog.Options.YesNo, Dialog.Message.Question, null)
    if (ok == Dialog.Result.Yes) {
      val status = ParserBank.deleteParser(parser)
      status match {
        case Some(parserName) =>
          updateParserChooser(parserName)
          ParserTreePanel.setParser(ParserBank(parserName))
          isDirty = true
        case None =>
      }
    }
  }

  def defineWhitespace() {
    Dialog.showInput(VisualLangLab.splitPane, "Whitespace regex", "Define whitespace", Dialog.Message.Question, null, Array[String](), Parsers.customWhitespace) match {
      case Some(whiteSpace) =>
        Parsers.customWhitespace = whiteSpace.asInstanceOf[String].r
        isDirty = true
      case None =>
    }
  }

  def defineComments() {
    Dialog.showInput(VisualLangLab.splitPane, "Comment regex", "Define comment", Dialog.Message.Question, null, Array[String](), Parsers.comments) match {
      case Some(comments) =>
        Parsers.comments = comments.asInstanceOf[String].r
        isDirty = true
      case None =>
    }
  }

  def updateParserChooser(selectedName: String) {
    parserChooser.removeActionListener(this)
    parserChooser.removeAllItems
    val parserNames = ParserBank.getParserNames
    parserNames.foreach(r => parserChooser.addItem(r))
    parserChooser.setSelectedItem(selectedName)
    parserChooser.addActionListener(this)
  }

  def actionPerformed(ae: ActionEvent) {
    ae.getSource match {
      case `parserChooser` => ParserTreePanel.setParser(ParserBank(parserChooser.getSelectedItem.asInstanceOf[String]))
    }
  }

  private val tk = java.awt.Toolkit.getDefaultToolkit
  val screenWidth = tk.getScreenSize.width
  val screenHeight = tk.getScreenSize.height
  val frameWidth = (screenWidth * 0.7).asInstanceOf[Int]
  val treeWidth = (screenWidth * 0.15).asInstanceOf[Int]
  val frameHeight = (screenHeight * 0.75).asInstanceOf[Int]
  // a token contains either a Left(literal) or a Right(regex)
  var currentParserName = "NoName"

  val fileNewMenuItem = new MenuItem("New") { reactions += {
      case ButtonClicked(_) =>
        saveChanges()
        if (!isDirty) {
          TokenBank.clear()
          ParserBank.clear()
          ParserBank("NoName") = new RootNode("NoName")
          updateParserChooser("NoName")
          ParserTreePanel.setParser(ParserBank("NoName"))
          grammarFile = None
          top.title = "VisualLangLab"
        }
        customTreeHandlerClassName = None
        customTreeHandler = None
        basicPrinterMenuItem.selected = true
    }}
  val fileOpenMenuItem = new MenuItem("Open") { reactions += {
      case ButtonClicked(_) =>
        saveChanges()
        if (!isDirty) {
          fileChooser.showOpenDialog(splitPane) match {
            case FileChooser.Result.Approve =>
              grammarFile = Some(fileChooser.selectedFile)
              FileIO.load(grammarFile.get)
              top.title = "VisualLangLab - " + grammarFile.get.getName
            case _ =>
          }
        }
    }}
  val fileSaveMenuItem = new MenuItem("Save") {reactions += {
      case ButtonClicked(_) =>
        grammarFile match {
          case Some(file) => FileIO.save(file)
          case None => fileChooser.showSaveDialog(splitPane) match {
              case FileChooser.Result.Approve =>
                grammarFile = Some(fileChooser.selectedFile)
                FileIO.save(grammarFile.get)
                top.title = "VisualLangLab - " + grammarFile.get.getName
              case _ =>
            }
        }
        isDirty = false
    }}
  val fileSaveAsMenuItem = new MenuItem("Save As") {reactions += {
      case ButtonClicked(_) =>
        fileChooser.showSaveDialog(splitPane) match {
          case FileChooser.Result.Approve =>
            grammarFile = Some(fileChooser.selectedFile)
            FileIO.save(grammarFile.get)
            isDirty = false
            top.title = "VisualLangLab - " + grammarFile.get.getName
          case _ =>
        }
    }}

  val fileExitMenuItem = new MenuItem("Exit") {
    reactions += {case ButtonClicked(_) =>
      saveChanges()
    }
  }
  val fileMenu = new Menu("File") {
    contents + fileNewMenuItem + new Separator + fileOpenMenuItem + fileSaveMenuItem +
    fileSaveAsMenuItem + new Separator + fileExitMenuItem
  }

  val newParserMenuItem = new MenuItem("New parser") {
    reactions += {case ButtonClicked(_) => createNewParser()}
  }
  val renameParserMenuItem = new MenuItem("Rename parser") {
    reactions += {case ButtonClicked(_) => renameParser()}
  }
  val deleteParserMenuItem = new MenuItem("Delete parser") {
    reactions += {case ButtonClicked(_) => deleteParser()}
  }
  val parserMenu = new Menu("Parsers") {
    contents + newParserMenuItem + renameParserMenuItem + new Separator + deleteParserMenuItem
  }

  val newLiteralMenuItem = new MenuItem("New literal") {
    reactions += {case ButtonClicked(_) => createNewToken(false)}
  }
  val newRegexMenuItem = new MenuItem("New regex") {
    reactions += {case ButtonClicked(_) => createNewToken(true)}
  }
  val editTokenMenuItem = new MenuItem("Edit token") {
    reactions += {case ButtonClicked(_) => editToken()}
  }
  val reviewTokenMenuItem = new MenuItem("Delete token") {
    reactions += {case ButtonClicked(_) => deleteToken()}
  }
  val tokensMenu = new Menu("Tokens") {
    contents + newLiteralMenuItem + newRegexMenuItem + editTokenMenuItem + new Separator +
    reviewTokenMenuItem
  }

  val defineCommentMenuItem = new MenuItem("Comment") {
    reactions += {case ButtonClicked(_) => defineComments()}
  }
  val defineSpaceMenuItem = new MenuItem("Whitespace") {
    reactions += {case ButtonClicked(_) => defineWhitespace()}
  }
  val editMenu = new Menu("Edit") {
    contents + defineSpaceMenuItem + defineCommentMenuItem
  }
  
  val lookNFeelMenuItem = new MenuItem("Look&Feel") {
    reactions += {case ButtonClicked(_) => manageLookNFeel()}
  }
  val viewMenu = new Menu("View") {
    contents + lookNFeelMenuItem
  }

  val runParserMenuItem = new MenuItem("Run parser") {
    reactions += {case ButtonClicked(_) =>
        val inputText = TextPane.getText
        val parserName = VisualLangLab.parserChooser.getSelectedItem.asInstanceOf[String]
        printf("%s Run start: %s %s%n", ("--" * 10), parserName, ("--" * 10))
        val parser = Parsers.createParserFor(parserName)
        val res = Parsers.parseAll(parser, inputText)
        res match {
          case Parsers.Success(tree, _) =>
            if (customHandlerMenuItem.selected) {
              customTreeHandler.get.onParse(tree)
            } else if (prettyPrinterMenuItem.selected) {
              prettyTreePrinter.onParse(tree)
            } else {
              basicTreePrinter.onParse(tree)
            }
          case _ => println(res)
        }
        printf("%s Run end: %s %s%n", ("--" * 10), parserName, ("--" * 10))
    }
  }
  val clearLogMenuItem = new MenuItem("Clear log") {
    reactions += {case ButtonClicked(_) =>
        TextPane.clearLogText()
    }
  }
  val basicPrinterMenuItem = new RadioMenuItem("Basic")
  val prettyPrinterMenuItem = new RadioMenuItem("Pretty")
  val customHandlerMenuItem = new RadioMenuItem("Custom") {
    reactions += {case ButtonClicked(_) =>
        val oldClassName = customTreeHandlerClassName match {
          case Some(cn) => cn
          case None => null
        }
        Dialog.showInput(VisualLangLab.splitPane, "Enter class-name", "Custom tree handler",
                         Dialog.Message.Question, null, Array[String](), oldClassName) match {
          case Some(className) =>
            try {
              customTreeHandler = Some(Class.forName(className).newInstance.asInstanceOf[ParseTreeHandler])
              customTreeHandlerClassName = Some(className)
            } catch {
              case ex =>
                Dialog.showMessage(splitPane, ex, "Custom tree handler", Dialog.Message.Error, null)
                basicPrinterMenuItem.selected = true
            }
          case _ => basicPrinterMenuItem.selected = true
        }
    }
  }
  val hdlrBtnGrp = new ButtonGroup(basicPrinterMenuItem, prettyPrinterMenuItem, customHandlerMenuItem)
  basicPrinterMenuItem.selected = true
  val treeHandlerMenu = new Menu("Tree handler") {
    contents + basicPrinterMenuItem + prettyPrinterMenuItem + customHandlerMenuItem
  }
  val flattenMenuItem = new CheckMenuItem("Flatten nested ~")
  val runMenu = new Menu("Run") {
    contents + runParserMenuItem + flattenMenuItem + treeHandlerMenu + new Separator +
    clearLogMenuItem
  }

  val aboutMenuItem = new MenuItem("About VisualLangLab") {
    val msg ="""
                VisualLangLab, Ver-2.01
      An IDE for LL(*) Parsers using Scala Combinators
Copyright 2010, Sanjay Dasgupta (sanjay.dasgupta@gmail.com)
              (https://vll.dev.java.net/)
"""
    reactions += {case ButtonClicked(_) =>
        Dialog.showMessage(splitPane, msg, "VisualLangLab", Dialog.Message.Info, null)
    }
  }
  val helpSampleItem1 = new MenuItem("ArithExpr") {
    reactions += {
      case ButtonClicked(_) => ArithExpr.load()
    }
  }
  val helpSampleItem2 = new MenuItem("SimpleJSON") {
    reactions += {
      case ButtonClicked(_) => SimpleJSON.load()
    }
  }
  val helpSampleItem3 = new MenuItem("Sample-3 ???")
  val helpSamples = new Menu("Samples") {
    contents + helpSampleItem1 + helpSampleItem2 + helpSampleItem3
  }
  val helpMenuItem = new MenuItem("Help") {
    val msg = "Not yet implemented\nCheck for documentation at https://vll.dev.java.net"
    reactions += {case ButtonClicked(_) =>
        Dialog.showMessage(splitPane, msg, "VisualLangLab", Dialog.Message.Info, null)
    }
  }
  val helpMenu = new Menu("Help") {
    contents + helpMenuItem + new Separator + helpSamples + new Separator + aboutMenuItem
  }

  val basicTreePrinter: ParseTreeHandler = BasicTreePrinter
  val prettyTreePrinter: ParseTreeHandler = PrettyTreePrinter
  var customTreeHandlerClassName: Option[String] = None
  var customTreeHandler: Option[ParseTreeHandler] = None
  val vllIconImage = swing.Swing.Icon(getClass.getResource("images/Icon.gif"))
  var isDirty = false
  val parserChooser = new JComboBox()
  parserChooser.addActionListener(this)
  val splitPane = new SplitPane(Orientation.Vertical, LeftContainerPanel, TextPane)
  var grammarFile: Option[File] = None
  val fileChooser = new FileChooser(new File(System.getProperty("user.dir"))) {
    fileSelectionMode = FileChooser.SelectionMode.FilesOnly
    multiSelectionEnabled = false
    fileFilter = new FileFilter() {
      def accept(f: File) = (f.getName endsWith ".vll") || f.isDirectory
      def getDescription = "VisualLangLab grammar"
    }
  }
  ParserBank("NoName") = ParserTreePanel.rootNode
  updateParserChooser("NoName")
  
  val top: MainFrame = new MainFrame {
    iconImage = vllIconImage.getImage
    title = "VisualLangLab"
    splitPane.dividerLocation = treeWidth
    contents = new BorderPanel {
      add(splitPane, BorderPanel.Position.Center)
      //add(new Label("Status"), BorderPanel.Position.South)
    }
    location = ((screenWidth - frameWidth) / 2, (screenHeight - frameHeight) / 2)
    preferredSize = (frameWidth, frameHeight)
    menuBar = new MenuBar {
      contents + fileMenu + editMenu + viewMenu + tokensMenu + parserMenu + runMenu + helpMenu
    }
    reactions += {
      case WindowClosing(w) => saveChanges()
    }
    System.setOut(new PrintStream(OutStream))
    System.setErr(new PrintStream(OutStream))
  }
}
