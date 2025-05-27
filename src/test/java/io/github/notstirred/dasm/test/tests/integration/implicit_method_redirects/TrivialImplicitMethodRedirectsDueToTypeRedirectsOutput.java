package io.github.notstirred.dasm.test.tests.integration.implicit_method_redirects;

public class TrivialImplicitMethodRedirectsDueToTypeRedirectsOutput {
    public void method1(Float a) {
        int hashCode = a.hashCode();
        System.out.printf("thing foo %d\n", hashCode);
    }

    public void method1(String a) {
        int hashCode = a.hashCode();
        System.out.printf("thing foo %d\n", hashCode);
    }
}
