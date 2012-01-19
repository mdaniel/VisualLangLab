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

/*
 The design of this class is inspired by the element of the same 
 name in the Scala (http://scala-lang.org/) standard library. 
 The Java code is also based on the corresponding Scala source.
 */

package net.java.vll.vll4j.combinator;

    public abstract class Option<T> {
        public abstract T get();
        public abstract boolean isEmpty();
    }

    class Some<T> extends Option<T> {
        public Some(T t) {this.t = t;}
        @Override
        public boolean isEmpty() {return false;}
        @Override
        public T get() {return t;}
        private T t;
    }

    class None<T> extends Option<T> {
        @Override
        public boolean isEmpty() {return true;}
        @Override
        public T get() {throw new UnsupportedOperationException();}
    }
    
