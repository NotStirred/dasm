package io.github.notstirred.dasm.test.tests.t2;

public class T2Input {
    public void method1(Object a) {
        int hashCode = a.hashCode();
        System.out.printf("thing foo %d\n", hashCode);
    }
}
