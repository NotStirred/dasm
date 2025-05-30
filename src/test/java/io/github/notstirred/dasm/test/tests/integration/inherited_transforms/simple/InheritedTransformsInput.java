package io.github.notstirred.dasm.test.tests.integration.inherited_transforms.simple;

import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;

public class InheritedTransformsInput {
    void method1() {
        Foo foo = new Foo();
        foo.fooField += 1;
        foo.fooField2 = foo.fooField2 - 1;
        foo.foo();
    }
}
