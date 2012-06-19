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

package net.java.vll.vll4j.gui;

import net.java.vll.vll4j.api.*;

import java.util.*;

public class VisitorFirstSets extends VisitorBase {

    public VisitorFirstSets(Forest theForest) {
        this.theForest = theForest;
    }

    @Override
    public Set<String>[] visitChoice(NodeChoice n) {
        int cc =  n.getChildCount();
        NodeBase cn[] = new NodeBase[cc];
        ArrayList<Set<String>> firsts = new ArrayList<Set<String>>();
        Set<String> cFirsts[][] = new Set[cc][];
        for (int i = 0; i < cc; ++i) {
            cn[i] = (NodeBase)n.getChildAt(i);
            cFirsts[i] = (Set<String>[])cn[i].accept(this);
            while (firsts.size() < cFirsts[i].length)
                firsts.add(new HashSet<String>());
        }
        for (int i = 0; i < cc; ++i) {
            int cfLen = cFirsts[i].length;
            for (int j = 0; j < cfLen; ++j) {
                firsts.get(j).addAll(cFirsts[i][j]);
                if (cn[i].multiplicity == Multiplicity.ZeroOrMore || cn[i].multiplicity == Multiplicity.OneOrMore) {
                    while (firsts.size() < LA)
                        firsts.add(new HashSet<String>());
                    for (int k = 1; j + cfLen * k < firsts.size(); ++k) {
                        firsts.get(j + cfLen * k).addAll(cFirsts[i][j]);
                        if (j == 0)
                            firsts.get(cfLen * k).add("");
                    }
                }
            }
            if (cfLen < firsts.size())
                firsts.get(cfLen).add("");
        }
        return firsts.toArray(new Set[firsts.size()]);
    }

    @Override
    public Set<String>[] visitLiteral(NodeLiteral n) {
        Set<String> ss = new HashSet<String>();
        ss.add(n.literalName);
        return new Set[] {ss};
    }

    @Override
    public Set<String>[] visitReference(NodeReference n) {
        String referredRuleName = n.refRuleName;
        NodeBase referredRule = theForest.ruleBank.get(referredRuleName);
        return (Set<String>[])referredRule.accept(this);
    }

    @Override
    public Set<String>[] visitRegex(NodeRegex n) {
        Set<String> ss = new HashSet<String>();
        ss.add(n.regexName);
        return new Set[] {ss};
    }

    @Override
    public Set<String>[] visitRepSep(NodeRepSep n) {
        if (n.getChildCount() == 2) {
            Set<String> rep[] = (Set<String>[])((NodeBase)n.getChildAt(0)).accept(this);
            Set<String> sep[] = (Set<String>[])((NodeBase)n.getChildAt(1)).accept(this);
            Set<String> retVal[] = Arrays.copyOf(rep, LA);
            for (int i = 0; i < LA; ++i) {
                if (retVal[i] == null)
                    retVal[i] = new HashSet<String>();
            }
            for (int k = 0; rep.length + k * (rep.length + sep.length) < LA; ++k) {
                int off = rep.length + k * (rep.length + sep.length);
                retVal[off].add("");
                for (int i = 0; i < sep.length && off + i < LA; ++i)
                    retVal[off + i].addAll(sep[i]);
                off += sep.length;
                for (int i = 0; i < rep.length && off + i < LA; ++i)
                    retVal[off + i].addAll(rep[i]);
            }
            if (n.multiplicity == Multiplicity.ZeroOrMore)
                retVal[0].add("");
            return retVal;
        } else {
            return new Set[0];
        }
    }

    @Override
    public Set<String>[] visitRoot(NodeRoot n) {
        if (activeRules.contains(n.getName())) {
            return new Set[0];
        } else {
            activeRules.add(n.getName());
            NodeBase child = (NodeBase)n.getFirstChild();
            Set<String>[] retVal = (Set<String>[])child.accept(this);
            activeRules.remove(n.getName());
            return retVal;
        }
    }

    @Override
    public Set<String>[] visitSemPred(NodeSemPred n) {
        return new Set[0];
    }

    private boolean mergeSequence(Set<String>[] from, List<Set<String>> to, NodeBase n) {
        int oldLength = to.size();
        int newLength = (n.multiplicity == Multiplicity.ZeroOrMore || n.multiplicity == Multiplicity.OneOrMore) ?
                LA : Math.min(LA, oldLength + from.length);
        for (int i = oldLength; i < newLength; ++i) {
            HashSet<String> hs = new HashSet<String>();
            if (i == oldLength)
                hs.add("");
            to.add(hs);
        }
        for (int i = newLength - 1; i >= 0; --i) {
            if (!to.get(i).contains(""))
                continue;
            if (n.multiplicity != Multiplicity.ZeroOrOne && n.multiplicity != Multiplicity.ZeroOrMore) {
                to.get(i).remove("");
            }
            if (i + from.length < newLength) {
                to.get(i + from.length).add("");
            }
            for (int j = 0; j < from.length && i + j < newLength; ++j) {
                int offs = i + j;
                to.get(offs).addAll(from[j]);
                if (n.multiplicity == Multiplicity.OneOrMore || n.multiplicity == Multiplicity.ZeroOrMore) {
                    for (int offs2 = offs + from.length; offs2 < newLength; offs2 += from.length) {
                        to.get(offs2).addAll(from[j]);
                        if (j == 0)
                            to.get(offs2).add("");
                    }
                }
            }
        }
        boolean more = false;
        for (int i = 0; !more && i < newLength; ++i)
            more = to.get(i).contains("");
        return more || newLength < LA;
    }

    @Override
    public Set<String>[] visitSequence(NodeSequence n) {
        List<Set<String>> firsts = new ArrayList<Set<String>>();
        for (int i = 0; i < n.getChildCount(); ++i) {
            NodeBase c = (NodeBase)n.getChildAt(i);
            Set<String> f[] = (Set<String>[])c.accept(this);
            if (!mergeSequence(f, firsts, c))
                break;
        }
        return firsts.toArray(new Set[firsts.size()]);
    }

    @Override
    public Set<String>[] visitWildCard(NodeWildCard n) {
        Set<String> ss = new HashSet<String>();
        ss.add("WildCard");
        return new Set[] {ss};
    }

    private Forest theForest;
    private final int LA = 5;
    private Set<String> activeRules = new HashSet<String>();
//    private String epsilon = "\u03B5";
}
