package io.github.notstirred.dasm.test.tests.t2;

public class T2Input {
    public void method1(Float a) {
        int hashCode = a.hashCode();
        System.out.printf("thing foo %d\n", hashCode);
    }
}
