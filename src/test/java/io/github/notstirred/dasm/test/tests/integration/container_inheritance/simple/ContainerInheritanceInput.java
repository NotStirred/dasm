package io.github.notstirred.dasm.test.tests.integration.container_inheritance.simple;

import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.targets.inherited_transforms.FooBaz;

public class ContainerInheritanceInput {
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
}
