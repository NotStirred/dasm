package io.github.notstirred.dasm.test.tests.inter_constructor_to_factory;

import java.io.File;

public class ConstructorToFactoryInput {
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

    public String method1out() {
        return null;
    }
}