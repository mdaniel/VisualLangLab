package tests;

import java.util.List;
import java.util.regex.Pattern;
import net.java.vll.vll4j.combinator.Parsers.ParseResult;
import net.java.vll.vll4j.combinator.Parsers.Parser;
import net.java.vll.vll4j.combinator.RegexParsers;
import net.java.vll.vll4j.combinator.Utils;

public class TestRegexParsers {
    public static void main(String args[]) {
//        System.out.println(System.getProperty("java.home"));
        RegexParsers me = new RegexParsers();
        Parser<CharSequence> nbr = me.regex(Pattern.compile("\\d+"));
        Parser<CharSequence> name = me.regex(Pattern.compile("[a-z]+"));
        Parser<CharSequence> semi = me.literal(";");
/*        Parser<? extends Object> line = me.sequence(0, name, nbr, semi);
        ParseResult pr = me.parseAll(line, "sanjay \t\r\n  100   ;  ");
        me.dumpResult(pr);
        System.out.println(Utils.dumpValue(pr.get(), true));
        Parser<List<Object[]>> choice = me.rep(me.choice(nbr, name, semi));
        pr = me.parseAll(choice, "sanjay \t\r\n  100   ;  ");
        me.dumpResult(pr);
        System.out.println(Utils.dumpValue(pr.get(), true));
*/    }
}
