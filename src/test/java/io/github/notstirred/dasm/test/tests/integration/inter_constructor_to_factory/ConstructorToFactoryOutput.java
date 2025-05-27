package io.github.notstirred.dasm.test.tests.integration.inter_constructor_to_factory;

import java.io.File;

public class ConstructorToFactoryOutput {
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
        return TestConstructorToFactory.createString();
    }

    public void method2out1() {
        File f = new File(TestConstructorToFactory.fromString("parent"), "child");
        System.out.println(f);
    }

    public void method2out2() {
        File f = TestConstructorToFactory.fromParentWithChild(new File("parent"), "child");
        System.out.println(f);
    }

    public void method3out() {
        File f = new File(new String("parent"), "child");
        System.out.println(f);
    }
}