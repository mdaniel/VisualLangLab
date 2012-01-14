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

package vll4j.tree;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import vll4j.core.PackratParsers;
import vll4j.core.Parsers;
import vll4j.core.Reader;

public class Forest {

    private void populateNode(Node xn, NodeBase pn) {
        NamedNodeMap attr = xn.getAttributes();
        if (attr.getNamedItem("Drop") != null) {
            pn.isDropped = true;
        }
        if (attr.getNamedItem("ActionText") != null) {
            pn.actionText = attr.getNamedItem("ActionText").getTextContent();
            String status = compileActionCode(pn);
            if (status != null) {
                throw new IllegalArgumentException(String.format(
                        "Action-Code error at '%s': %s%n", pn.nodeName(), status));
            }
        }
        if (attr.getNamedItem("Description") != null) {
            pn.description = attr.getNamedItem("Description").getTextContent();
        }
        if (attr.getNamedItem("ErrMsg") != null) {
            pn.errorMessage = attr.getNamedItem("ErrMsg").getTextContent();
        }
        if (attr.getNamedItem("Mult") != null) {
            String m = attr.getNamedItem("Mult").getTextContent();
            if (m.equals("*")) {
                pn.multiplicity = Multiplicity.ZeroOrMore;
            } else if (m.equals("+")) {
                pn.multiplicity = Multiplicity.OneOrMore;
            } else if (m.equals("?")) {
                pn.multiplicity = Multiplicity.ZeroOrOne;
            } else if (m.equals("0")) {
                pn.multiplicity = Multiplicity.Not;
            } else if (m.equals("=")) {
                pn.multiplicity = Multiplicity.Guard;
            }
        }
        if (pn instanceof NodeChoice) {
        } else if (pn instanceof NodeLiteral) {
        } else if (pn instanceof NodeReference) {
        } else if (pn instanceof NodeRegex) {
        } else if (pn instanceof NodeRepSep) {
        } else if (pn instanceof NodeRoot) {
            if (attr.getNamedItem("Packrat") != null) {
                ((NodeRoot) pn).isPackrat = true;
            }
        } else if (pn instanceof NodeSequence) {
        }
        NodeList clist = xn.getChildNodes();
        for (int i = 0; i < clist.getLength(); ++i) {
            Node cn = clist.item(i);
            String elmtName = cn.getNodeName();
            if (elmtName.equals("Choice")) {
                NodeBase c = new NodeChoice();
                pn.add(c);
                populateNode(cn, c);
            } else if (elmtName.equals("Token")) {
                String tokenName = cn.getAttributes().getNamedItem("Ref").getTextContent();
                String tokenValue = tokenBank.get(tokenName);
                NodeBase c = tokenValue.charAt(0) == 'L' ? new NodeLiteral(tokenName)
                        : new NodeRegex(tokenName);
                pn.add(c);
                populateNode(cn, c);
            } else if (elmtName.equals("Reference")) {
                String ruleName = cn.getAttributes().getNamedItem("Ref").getTextContent();
                NodeBase c = new NodeReference(ruleName);
                pn.add(c);
                populateNode(cn, c);
            } else if (elmtName.equals("RepSep")) {
                NodeBase c = new NodeRepSep();
                pn.add(c);
                populateNode(cn, c);
            } else if (elmtName.equals("Root")) {
                // Never found
            } else if (elmtName.equals("Sequence")) {
                NodeBase c = new NodeSequence();
                pn.add(c);
                populateNode(cn, c);
            }
        }
    }

    public void openInputStream(InputStream is, boolean onlyTokens) throws ParserConfigurationException, 
            SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.parse(is);
        Element docElmt = d.getDocumentElement();
        NodeList regs = docElmt.getElementsByTagName("Regex");
        for (int i = 0; i < regs.getLength(); ++i) {
            Node r = regs.item(i);
            String regName = r.getAttributes().getNamedItem("Name").getTextContent();
            String regPat = r.getAttributes().getNamedItem("Pattern").getTextContent();
            tokenBank.put(regName, "R" + regPat);
        }
        NodeList lits = docElmt.getElementsByTagName("Literal");
        for (int i = 0; i < lits.getLength(); ++i) {
            Node r = lits.item(i);
            String litName = r.getAttributes().getNamedItem("Name").getTextContent();
            String litPat = r.getAttributes().getNamedItem("Pattern").getTextContent();
            tokenBank.put(litName, "L" + litPat);
        }
        if (onlyTokens)
            return;
        whiteSpace = docElmt.getElementsByTagName("Whitespace").item(0).getTextContent();
        comment = docElmt.getElementsByTagName("Comments").item(0).getTextContent();
        NodeList parsers = docElmt.getElementsByTagName("Parser");
        for (int i = 0; i < parsers.getLength(); ++i) {
            Node xNode = parsers.item(i);
            String ruleName = xNode.getAttributes().getNamedItem("Name").getTextContent();
            NodeRoot root = new NodeRoot(ruleName);
            populateNode(xNode, root);
            ruleBank.put(ruleName, root);
        }
    }

    private ActionFunction compile(String script) throws ScriptException {
        if (compilable == null) {
            scriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
            compilable = (Compilable) (scriptEngine);
        }
        script = script.substring(script.indexOf('('));
        final CompiledScript cs = compilable.compile(String.format("(function %s)(vllARG)", script));
        return new ActionFunction() {
            @Override
            public Object run(Object arg, Reader r) throws ScriptException {
                cs.getEngine().put("vllARG", arg);
                cs.getEngine().put("vllLine", r.line());
                cs.getEngine().put("vllCol", r.column());
                cs.getEngine().put("vllOffset", r.offset());
                cs.getEngine().put("vllInput", r.source().subSequence(r.offset(), r.source().length()));
                cs.getEngine().put("vllLastNoSuccess", PackratParsers.lastNoSuccess);
                for (String k : bindings.keySet()) {
                    cs.getEngine().put(k, bindings.get(k));
                }
                return cs.eval();
            }
        };
    }

    public String compileActionCode(NodeBase node) {
        String script = node.actionText;
        if (script.trim().isEmpty()) {
            node.actionFunction = null;
            return null;
        } else {
            if (!functionMatcher.reset(script).matches()) {
                node.actionFunction = null;
                return "Need JavaScript function with 1 argument";
            }
            try {
                node.actionFunction = compile(script);
                return null;
            } catch (Exception e) {
                node.actionFunction = null;
                String message = e.getMessage();
                message = message.contains(": ") ? message.substring(message.indexOf(": ") + 2) : message;
                return message;
            }
        }
    }
    
    ScriptEngine scriptEngine = null;
    Compilable compilable = null;
    private Matcher functionMatcher = Pattern.compile(
            "\\s*f(?:u(?:n(?:c(?:t(?:i(?:on?)?)?)?)?)?)?\\s*\\(\\s*[a-zA-Z][a-zA-Z0-9]*\\s*\\)(?s:.*)").matcher("");
    public Map<String, String> tokenBank = new TreeMap<String, String>();
    public Map<String, NodeBase> ruleBank = new TreeMap<String, NodeBase>();
    public String whiteSpace, comment;
    public Map<String, Object> bindings = new HashMap<String, Object>();
}
