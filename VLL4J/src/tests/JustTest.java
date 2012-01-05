package tests;

public class JustTest {
    public static void main(String args[]) {
        String msg = "function (hello) {return;";
        boolean m = msg.matches("function\\s*\\([a-zA-Z][a-zA-Z0-9]*\\)\\s*\\{.+\\}");
        System.out.println(m);
    }
}
