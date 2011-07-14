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


import java.io.OutputStream

class OutStream(tPane: LogTextPane, isError: Boolean) extends OutputStream {

  override def write(buffer: Array[Byte], offset: Int, length: Int) = {
    if (isError)
      tPane.logError(new String(buffer, offset, length))
    else
      tPane.logOutput(new String(buffer, offset, length))
    length
  }

  def write(c: Int) {
    val chArray = Array(c.asInstanceOf[Byte])
    write(chArray, 0, 1)
  }
  
}
