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

import java.awt.Cursor
import java.io.File
import scala.actors.DaemonActor
import scala.collection.immutable.WrappedString
import scala.io.Source
import scala.swing.Dialog
import scala.util.parsing.input.CharSequenceReader
import vll.core.Utils
import vll.core.VllParsers

class ParsingActor(gui: VllGui) extends VllParsers with DaemonActor {
//  gui.globalFlattenTilde.selected = flattenNestedTilde
  super[DaemonActor].start()

  private def dredgeFiles(f: File): List[File] = {
    if (f.isDirectory) {
      f.listFiles.toList.flatMap(dredgeFiles)
    } else {
      List(f)
    }
  }

  private def handleTree(tree: Any) {
    if (gui.runHandlerBasicMenuItem.selected) {
      basicTreePrinter(tree)
    } else if (gui.runHandlerStructuredMenuItem.selected) {
      structuredTreePrinter(tree)
    } else {
      gui.customTreeHandler.get.onParse(this, tree)
    }
  }

  private def file2string(f: File) = {
    if (f.length == 0) {
      ""
    } else {
      val src = Source.fromFile(f)
      val sb = new StringBuilder()
      for (line <- src.getLines()) {
        sb.append(line).append("\n")
      }
      src.close()
      sb.toString
    }
  }

  private def parseFile(file: File) {
    gui.stopButton.enabled = true
    gui.parsers.userRequestedStop = false
    val defaultCursor = gui.cursor
    gui.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    var i, errors = 0
    val ruleName = gui.ruleChooser.getSelectedItem.asInstanceOf[String]
    val parser = gui.parsers.getParserFor(ruleName)
    if (parser != null) {
      if (file.exists) {
        val startTime = System.currentTimeMillis
        for (f <- dredgeFiles(file)) if (!gui.parsers.userRequestedStop) {
          i += 1
          if (file.isDirectory)
            printf("%4d %s ", i, f.getAbsolutePath)
          else
            printf("%s ", f.getAbsolutePath)
          fileToParse = f
          val text = file2string(f)
          printf("(%d chars) ", text.length)
          val startTime = System.currentTimeMillis
          try {
            val result = gui.parsers.phrase(parser/*  <~ gui.parsers.eofParser */)(new CharSequenceReader(text))
            val duration = (System.currentTimeMillis - startTime)
            result match {
              case gui.parsers.Success(tree, _) =>
                val fm = Runtime.getRuntime.freeMemory
                val mm = Runtime.getRuntime.maxMemory
                printf(": %s (%d ms, free-mem=%4.1f%%)%n", result.getClass.getSimpleName, duration,
                       (fm * 100f) / mm, Runtime.getRuntime.maxMemory)
                if (file.isFile)
                  handleTree(tree)
              case gui.parsers.NoSuccess(msg, rest) =>
/*                 val res = gui.parsers.globalTokenParser(rest) match {
                  case gui.parsers.Success(str, _) => Utils.reEscape(str._1)
                  case gui.parsers.NoSuccess(_, _) => null
                };
 */                System.err.printf(": %s: %s, found [%s] at (%d, %d)%n", result.getClass.getSimpleName, msg,
                       //(if (res != null) res else
                       Utils.reEscape(rest.source.subSequence(rest.offset, math.min(rest.source.length, rest.offset + 15))),
                      rest.pos.line.asInstanceOf[AnyRef], rest.pos.column.asInstanceOf[AnyRef]);
                errors += 1
            }
          } catch {
            case x => 
              if (gui.parsers.userRequestedStop)
                System.err.println("\nUser-requested STOP")
              else {
                x.printStackTrace()
                errors += 1
              }
          }
        }
        val endTime = System.currentTimeMillis
        if (file.isDirectory)
          printf("%d files, %d errors in %d ms (%d,%d)%n", i, errors, endTime - startTime,
                 gui.parsers.globalTokenParserTime, gui.parsers.globalTokenParserTime2)
      } else
        System.err.printf("No such file: %s%n", file.getAbsolutePath)
    } else {
        Dialog.showMessage(null, "Errors in parser prevent run", "Run Error", Dialog.Message.Error, null)
        System.err.printf("Errors in parser prevent run%n")
    }
    gui.cursor = defaultCursor
    gui.stopButton.enabled = false
  }

  private def parseInput() {
    gui.stopButton.enabled = true
    gui.parsers.userRequestedStop = false
    val defaultCursor = gui.cursor
    gui.cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
    val ruleName = gui.ruleChooser.getSelectedItem.asInstanceOf[String]
    printf("Generating parsers ... ")
    startTime = System.currentTimeMillis
    val parser = gui.parsers.getParserFor(ruleName)
    if (parser != null) {
      combinatorEndTime = System.currentTimeMillis
      printf("(%d ms)%nParsing ... ", combinatorEndTime - startTime)
      val theReader = new CharSequenceReader(new TextComponentCharSequence(gui.logTextPane.inputArea.peer))
      var message: Option[String] = None
      try {
        val res = gui.parsers.phrase(parser)(theReader)
        parseEndTime = System.currentTimeMillis;
        val duration = parseEndTime - combinatorEndTime
        res match {
           case gui.parsers.Success(tree, rest) =>
            printf("(%d chars in %d ms), result follows:%n", theReader.source.length, duration)
             handleTree(tree)
           case gui.parsers.NoSuccess(msg, rest) =>
/*                 val res2 = gui.parsers.globalTokenParser(rest) match {
                  case gui.parsers.Success(str, _) => Utils.reEscape(str._1)
                  case gui.parsers.NoSuccess(_, _) => null
                }
 */                System.err.printf(": %s: %s, found [%s] at (%d, %d)%n", res.getClass.getSimpleName,
                       if (msg == null) "?" else msg, //if (res2 != null) res2 else
                       Utils.reEscape(rest.source.subSequence(rest.offset, math.min(rest.source.length, rest.offset + 15))),
                     rest.pos.line.asInstanceOf[AnyRef], rest.pos.column.asInstanceOf[AnyRef])
        }
      } catch {
        case x =>
          if (gui.parsers.userRequestedStop)
            System.err.println("\nUser-requested STOP")
          else
            x.printStackTrace()
            //printf("Exception: %s(%s)%n", x.getClass.getName, x.getMessage)
      }
    } else {
        Dialog.showMessage(null, "Errors in parser prevent run", "Run Error", Dialog.Message.Error, null)
        System.err.printf(": Errors in parser prevent run%n")
    }
    gui.cursor = defaultCursor
    gui.stopButton.enabled = false
  }

  var startTime: Long = 0
  var combinatorEndTime: Long = 0
  var parseEndTime: Long = 0

  def basicTreePrinter(tree: Any) {
    def asString(v: Any): String = {
      v match {
        //case arr: IndexedSeq[_] => arr.map(asString).mkString("IndexedSeq(", ", ", ")")
        case arr: Array[_] => arr.map(asString).mkString("Array(", ", ", ")")
        case lst: List[_] => lst.map(asString).mkString("List(", ", ", ")")
        case Some(s) => "Some(%s)".format(asString(s))
        case None => "None"
        //case Tuple2(a, b) => format("Tuple2(%s, %s)", asString(a), asString(b))
        case Pair(a, b) => "Pair(%s, %s)".format(asString(a), asString(b))
        case null => "null"
        case x => x.toString
      }
    }
    println(asString(tree))
  }
  
  def structuredTreePrinter(tree: Any) {
    def structPrint(t: Any, level: Int) {
      val margin: String = new WrappedString("    ") * level
      t match {
        case None => printf("%sNone%n", margin)
        case Some(s) =>
          printf("%sSome(%n", margin)
          structPrint(s, level + 1)
          printf("%s)%n", margin)
        case lst: List[_] =>
          printf("%sList(%n", margin)
          lst.foreach(structPrint(_, level + 1))
          printf("%s)%n", margin)
//        case a: IndexedSeq[_] =>
//          printf("%sIndexedSeq(%n", margin)
//          a.foreach(structPrint(_, level + 1))
        case a: Array[_] =>
          printf("%sArray(%n", margin)
          a.foreach(structPrint(_, level + 1))
          printf("%s)%n", margin)
        case Pair(a, b) =>
          printf("%sPair(%n", margin)
          structPrint(a, level + 1)
          structPrint(b, level + 1)
          printf("%s)%n", margin)
        case _ => printf("%s%s%n", margin, t)
      }
    }
    structPrint(tree, 0)
  }

  def act() {
    def sample(s: String) = if (s.length > 10) (s.substring(0, 10) + "...") else s
    loop {
      receive {
        case file: File => parseFile(file)
        case _ => parseInput()
      }
    }
  }
}

