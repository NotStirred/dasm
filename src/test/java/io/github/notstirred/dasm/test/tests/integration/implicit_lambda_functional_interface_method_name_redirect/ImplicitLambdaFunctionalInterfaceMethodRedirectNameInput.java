package io.github.notstirred.dasm.test.tests.integration.implicit_lambda_functional_interface_method_name_redirect;

import io.github.notstirred.dasm.test.targets.functional_interface.IFoo;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;

public class ImplicitLambdaFunctionalInterfaceMethodRedirectNameInput {
    public void method1() {
        IFoo foo = f -> {
            System.out.println(f);
        };

        foo.foo(new Foo());
    }
}
