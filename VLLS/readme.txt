            VisualLangLab - An IDE for Parser Development

                       (http://vll.java.net/)

Copyright (c) 2010, Sanjay Dasgupta (sanjay.dasgupta@gmail.com)

VisualLangLab is a completely visual IDE for development of parsers without 
code or scripts of any kind. It is simple in use, and very easy to learn. 
A grammar is represented as a tree with distinct icons for the grammar elements.

The grammar tree is executable, and can be run at any time at the click of a 
button -- without delay or further manual intervention. 
Test input for the parser can be entered directly into the IDE or 
obtained from user-specified files. 
Running the parser does not require any other tools or skills. 
This capability simplifies testing, and encourages the use of an incremental, 
iterative development process.

Parser development is reduced to editing the tree's structure, 
using the mouse for menu-bar and context-menu operations. 
Textual input from the keyboard is required only for naming parsers and tokens, 
and for specifying regular expression patterns for tokens. 

The grammar is saved as an intuitive XML file that can be loaded again later 
for further editing and testing. An API is also available for client programs 
(in Scala or Java) to load the saved XML file and regenerate the parser. 
Parser regeneration happens on the fly, (when the XML file-name is passed to the API) 
and does not require any intermediate steps or other manual intervention. 

The software uses Scala's parallel-collections API, so version 2.9.0+ is required.

This software is released under the GNU General Public License. 
See the file "copying.txt" for further details. For more information, go to: 
    http://vll.java.net/
