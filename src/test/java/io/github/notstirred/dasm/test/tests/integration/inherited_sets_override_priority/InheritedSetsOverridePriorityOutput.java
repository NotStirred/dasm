package io.github.notstirred.dasm.test.tests.integration.inherited_sets_override_priority;

import io.github.notstirred.dasm.test.targets.inherited_transforms.BarBaz;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.targets.inherited_transforms.FooBaz;

public class InheritedSetsOverridePriorityOutput {
    public void method1() {
        new Foo().foo();
    }

    public void method1out1() {
        new BarBaz().barbaz();
    }

    public void method1out2() {
        new FooBaz().foobaz();
    }
}
