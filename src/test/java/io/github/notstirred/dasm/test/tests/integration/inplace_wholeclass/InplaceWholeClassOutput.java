package io.github.notstirred.dasm.test.tests.integration.inplace_wholeclass;

import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;

public class InplaceWholeClassOutput {
    B method1() {
        B a = new B();
        a.doBThings();

        return a;
    }

    Bar method2() {
        Bar foo = new Bar();
        foo.bar();

        foo.barField += 1;

        return foo;
    }
}
