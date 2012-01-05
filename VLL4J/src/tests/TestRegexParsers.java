package tests;

import java.util.List;
import vll4j.core.Parsers.ParseResult;
import vll4j.core.Parsers.Parser;
import vll4j.core.RegexParsers;

public class TestRegexParsers {
    public static void main(String args[]) {
//        System.out.println(System.getProperty("java.home"));
        RegexParsers me = new RegexParsers();
        Parser<String> nbr = me.regex("\\d+");
        Parser<String> name = me.regex("[a-z]+");
        Parser<String> semi = me.literal(";");
        Parser<Object[]> line = me.sequence(0, name, nbr, semi);
        ParseResult pr = me.parseAll(line, "sanjay \t\r\n  100   ;  ");
        me.dumpResult(pr);
        System.out.println(me.dumpValue(pr.get()));
        Parser<List<Object[]/*<Object>*/>> choice = me.rep(me.choice(nbr, name, semi));
        pr = me.parseAll(choice, "sanjay \t\r\n  100   ;  ");
        me.dumpResult(pr);
        System.out.println(me.dumpValue(pr.get()));
    }
}
