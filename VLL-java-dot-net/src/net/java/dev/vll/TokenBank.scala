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

object TokenBank {
  def deleteToken(tokenName: String) {
    tokens -= tokenName
    println("Deleting token: " + tokenName)
  }
  def contains(key: String) = tokens.contains(key)
  def iterator = tokens.iterator
  def isEmpty = tokens.isEmpty
  def getTokenNames = tokens.keySet.toSeq
  def get(tokenName: String) = tokens.get(tokenName)
  def getOrElse(tokenName: String, n: ParserTreeNode) = tokens.getOrElse(tokenName, n)
  def apply(tokenName: String) = tokens.apply(tokenName)
  def +=(kv: Pair[String, Either[String, String]]) = tokens += kv
  def update(tokenName: String, n: Either[String, String]) = this += (tokenName, n)
  def clear(): Unit = tokens = TreeMap[String, Either[String, String]]()
  private var tokens = TreeMap[String, Either[String, String]]()
}
