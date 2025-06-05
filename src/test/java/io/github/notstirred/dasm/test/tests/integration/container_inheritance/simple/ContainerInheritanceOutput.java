package io.github.notstirred.dasm.test.tests.integration.container_inheritance.simple;

import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.BarBaz;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.targets.inherited_transforms.FooBaz;

public class ContainerInheritanceOutput {
    void method1() {
        Foo foo = new Foo();
        foo.fooField += 1;
        foo.fooField2 = foo.fooField2 - 1;
        foo.foo();

        FooBaz fooBaz = new FooBaz();
        fooBaz.fooField += 1;
        fooBaz.fooField2 = fooBaz.fooField2 - 1;
        fooBaz.foo();
        fooBaz.foobaz();

        ((Foo) fooBaz).foo();
    }

    void method1out() {
        Bar bar = Bar.create();
        bar.barField += 1;
        bar.setBarField2(bar.getBarField2() - 1);
        bar.bar();

        BarBaz barBaz = BarBaz.create();
        barBaz.barField += 1;
        barBaz.setBarField2(barBaz.getBarField2() - 1);
        barBaz.bar();
        barBaz.barbaz();

        ((Bar) barBaz).bar();
    }
}
