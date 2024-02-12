package io.github.notstirred.dasm.test.tests.t6;

import java.io.File;

public class T6Output {
    public Object method1() {
        return new Object();
    }

    public void method2() {
        File f = new File(new File("parent"), "child");
        System.out.println(f);
    }

    public String method1out() {
        return T6Dasm.createString();
    }

    public void method2out1() {
        File f = new File(T6Dasm.fromString("parent"), "child");
        System.out.println(f);
    }

    public void method2out2() {
        File f = T6Dasm.fromParentWithChild(new File("parent"), "child");
        System.out.println(f);
    }
}