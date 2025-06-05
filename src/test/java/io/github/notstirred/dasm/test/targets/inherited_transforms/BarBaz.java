package io.github.notstirred.dasm.test.targets.inherited_transforms;

public class BarBaz extends Bar {
    public void barbaz() {
    }

    public static BarBaz create() {
        return new BarBaz();
    }
}
