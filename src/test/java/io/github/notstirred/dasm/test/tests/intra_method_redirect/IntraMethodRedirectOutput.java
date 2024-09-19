package io.github.notstirred.dasm.test.tests.intra_method_redirect;

public class IntraMethodRedirectOutput {
    public void method1(String a) {
        int i = new Object().hashCode();
        int val = a.hashCode();
        System.out.printf("thing foo %d\n", val);
    }

    public void method1out(String a) {
        int i = new Object().hashCode();
        int val = a.length();
        System.out.printf("thing foo %d\n", val);
    }
}