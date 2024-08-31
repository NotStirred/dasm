package io.github.notstirred.dasm.test.targets;

public class NewSoup {
    public int a;
    public int b;

    public NewSoup() { }

    public NewSoup(int foo) {
    }

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

    public static NewSoup create() {
        return new NewSoup();
    }

    public static NewSoup create(int foo) {
        return new NewSoup(foo);
    }
}
