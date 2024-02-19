package io.github.notstirred.dasm.test.tests.implicit_method_redirects;

public class TrivialImplicitMethodRedirectsDueToTypeRedirectsInput {
    public void method1(Float a) {
        int hashCode = a.hashCode();
        System.out.printf("thing foo %d\n", hashCode);
    }
}
