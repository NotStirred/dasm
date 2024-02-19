package io.github.notstirred.dasm.test.tests.method_redirect;

public class TrivialMethodRedirectInput {
    public void method1(String a) {
        int val = a.hashCode();
        System.out.printf("thing foo %d\n", val);
    }
}
