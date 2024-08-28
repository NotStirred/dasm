package io.github.notstirred.dasm.test.targets;

public class Soup {
    public int a;
    public int b;

    public Soup() { }

    public static int A = 1;
    public static int B = 2;

    public void foo1() {
    }

    public void foo2() {
    }

    public static void static_foo1() {
    }

    public static void static_foo2() {
    }

    public static Soup create() {
        return new Soup();
    }
}
