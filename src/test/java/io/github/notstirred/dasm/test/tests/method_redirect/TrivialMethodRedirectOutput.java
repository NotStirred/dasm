package io.github.notstirred.dasm.test.tests.method_redirect;

public class TrivialMethodRedirectOutput {
    public void method1(String a) {
        int val = a.hashCode();
        System.out.printf("thing foo %d\n", val);
    }

    public void method2(String a) {
        int val = a.length();
        System.out.printf("thing foo %d\n", val);
    }
}