package io.github.notstirred.dasm.test.targets.inherited_transforms;

import io.github.notstirred.dasm.test.targets.functional_interface.IFoo;

public class Foo implements IFoo {
    public int fooField;
    public int fooField2;

    public void foo() {
    }

    public void bar() {
    }

    @Override
    public void foo(Foo foo) {
    }
}
