package io.github.notstirred.dasm.test.tests.integration.inherited_transforms.simple;

import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;

public class InheritedTransformsOutput {
    void method1() {
        Foo foo = new Foo();
        foo.fooField += 1;
        foo.fooField2 = foo.fooField2 - 1;
        foo.foo();
    }

    void method1out() {
        Bar bar = Bar.create();
        bar.barField += 1;
        bar.setBarField2(bar.getBarField2() - 1);
        bar.bar();
    }
}
