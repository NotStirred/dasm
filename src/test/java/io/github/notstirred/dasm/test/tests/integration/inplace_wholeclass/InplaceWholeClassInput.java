package io.github.notstirred.dasm.test.tests.integration.inplace_wholeclass;

import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;

public class InplaceWholeClassInput {
    A method1() {
        A a = new A();
        a.doAThings();

        return a;
    }

    Foo method2() {
        Foo foo = new Foo();
        foo.foo();

        foo.fooField += 1;

        return foo;
    }
}
