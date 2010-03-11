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

import scala.collection.immutable.TreeMap
import scala.collection.mutable.Stack
import scala.swing.Dialog

object ParserBank {

  def deleteParser(parserName: String): Option[String] = {
    if (parsers.size == 1) {
      Dialog.showMessage(VisualLangLab.splitPane, "Can't delete last parser",
          "Delete parser", Dialog.Message.Error, null)
      None
    } else {
      parserInUse(parserName) match {
        case Nil =>
          val parserNames = parsers.keysIterator.toSeq
          val idx = if (parsers.size == 1) -1 else parserNames.indexOf(parserName)
          if (idx == -1)
            None
          else {
            parsers -= parserName
            if (parsers.size == 1) Some(parsers.keysIterator.next) else Some(parserNames(Math.max(1, idx - 1)))
          }
        case users: List[String] =>
              val msg = "Can't delete '%s' - used by: \n%s".format(parserName, users.mkString(",\n"))
              Dialog.showMessage(VisualLangLab.splitPane, msg, "Delete parser", Dialog.Message.Error, null)
          None
      }
    }
  }

  def tokenInUse(tokenName: String): List[String] = {
    var userList = Set[String]()
    var stack = new Stack[ParserTreeNode]()
    def usesToken(node: ParserTreeNode, tokenName: String) {
      node match {
        case ReferenceNode(m, pn) =>
          val refdParser = parsers(pn)
          if (!stack.contains(refdParser)) {
            stack.push(refdParser)
            usesToken(refdParser, tokenName)
          }
        case LiteralNode(m, tn) => if (tn == tokenName) userList += node.nodeName
        case RegexNode(m, tn) => if (tn == tokenName) userList += node.nodeName
        case s: SequenceNode =>
          for (i <- 0 until s.getChildCount) {
            usesToken(s.getChild(i), tokenName)
          }
        case c: ChoiceNode =>
          for (i <- 0 until c.getChildCount) {
            usesToken(c.getChild(i), tokenName)
          }
        case r: RepSepNode =>
          if (r.getChildCount > 0)
            usesToken(r.getChild(0), tokenName)
          if (r.getChildCount > 1)
            usesToken(r.getChild(1), tokenName)
        case r: RootNode =>
          if (r.getChildCount > 0)
            usesToken(r.getChild(0), tokenName)
      }
    }
    for (parser <- parsers.valuesIterator) {
      stack.clear
      stack.push(parser)
      usesToken(parser, tokenName)
    }
    userList.toList
  }

  def parserInUse(parserName: String): List[String] = {
    var userList = Set[String]()
    var stack = new Stack[ParserTreeNode]()
    def usesParser(node: ParserTreeNode, parserName: String) {
      node match {
        case ReferenceNode(m, pn) =>
          if (pn == parserName) userList += node.nodeName
          val refdParser = parsers(pn)
          if (!stack.contains(refdParser)) {
            stack.push(refdParser)
            usesParser(refdParser, parserName)
          }
        case LiteralNode(m, n) =>
        case RegexNode(m, n) =>
        case s: SequenceNode =>
          for (i <- 0 until s.getChildCount) {
            usesParser(s.getChild(i), parserName)
          }
        case c: ChoiceNode =>
          for (i <- 0 until c.getChildCount) {
            usesParser(c.getChild(i), parserName)
          }
        case r: RepSepNode =>
          if (r.getChildCount > 0)
            usesParser(r.getChild(0), parserName)
          if (r.getChildCount > 1)
            usesParser(r.getChild(1), parserName)
        case r: RootNode =>
          if (r.getChildCount > 0)
            usesParser(r.getChild(0), parserName)
      }
    }
    for (parser <- parsers.valuesIterator) {
      stack.clear
      stack.push(parser)
      usesParser(parser, parserName)
    }
    userList.toList
  }

  def contains(key: String) = parsers.contains(key)
  def iterator = parsers.iterator
  def isEmpty = parsers.isEmpty
  def getParserNames = parsers.keySet.toSeq
  def get(parserName: String) = parsers.get(parserName)
  def getOrElse(parserName: String, n: ParserTreeNode) = parsers.getOrElse(parserName, n)
  def apply(parserName: String) = parsers.apply(parserName)
  def +=(kv: Pair[String, ParserTreeNode]) = parsers += kv
  def update(parserName: String, n: ParserTreeNode) = this += (parserName, n)
  def clear(): Unit = parsers = TreeMap[String, ParserTreeNode]()
  private var parsers = TreeMap[String, ParserTreeNode]()
  
}
