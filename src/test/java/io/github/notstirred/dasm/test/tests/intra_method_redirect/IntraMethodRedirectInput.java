package io.github.notstirred.dasm.test.tests.intra_method_redirect;

public class IntraMethodRedirectInput {
    public void method1(String a) {
        int val = a.hashCode();
        System.out.printf("thing foo %d\n", val);
    }
}
