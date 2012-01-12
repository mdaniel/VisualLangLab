package tests;

import java.util.Stack;

public class JustTest {
    private static void one() {
        String msg = "function (hello) {return;";
        boolean m = msg.matches("function\\s*\\([a-zA-Z][a-zA-Z0-9]*\\)\\s*\\{.+\\}");
        System.out.println(m);
    }
    private static void two() {
        Stack<String> stack = new Stack<String>();
        stack.push("one"); stack.push("two"); stack.push("five"); stack.push("ten");
        for (String s: stack)
            System.out.println(s);
    }
    public static void main(String args[]) {
        two();
    }
}
