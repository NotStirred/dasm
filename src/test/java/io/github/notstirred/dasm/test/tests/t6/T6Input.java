package io.github.notstirred.dasm.test.tests.t6;

import java.io.File;

public class T6Input {
    public Object method1() {
        return new Object();
    }

    public void method2() {
        File f = new File(new File("parent"), "child");
        System.out.println(f);
    }

    public void method3() {
        File f = new File(new String("parent"), "child");
        System.out.println(f);
    }
}