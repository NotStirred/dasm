package io.github.notstirred.dasm.test.tests.t3;

public class T3Output {
    public void method1(String a) {
        int val = a.hashCode();
        System.out.printf("thing foo %d\n", val);
    }

    public void method2(String a) {
        int val = a.length();
        System.out.printf("thing foo %d\n", val);
    }
}