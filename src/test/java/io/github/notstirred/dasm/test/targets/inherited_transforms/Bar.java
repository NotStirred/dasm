package io.github.notstirred.dasm.test.targets.inherited_transforms;

public class Bar {
    public static Bar instance;

    public int barField;
    public int barField2;

    public void bar() {
    }

    public static Bar create() {
        return new Bar();
    }

    public void setBarField2(int barField2) {
    }

    public int getBarField2() {
        return barField2;
    }
}
