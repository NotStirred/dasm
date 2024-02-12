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
        File f;
        if (true) {
            f = new File((File) null, "child");
        } else {
            f = new File("foo");
        }
        System.out.println(f);
    }
}