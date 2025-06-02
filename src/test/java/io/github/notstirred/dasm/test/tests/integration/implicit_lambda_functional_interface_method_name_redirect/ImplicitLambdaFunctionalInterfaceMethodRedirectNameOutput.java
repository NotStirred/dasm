package io.github.notstirred.dasm.test.tests.integration.implicit_lambda_functional_interface_method_name_redirect;

import io.github.notstirred.dasm.test.targets.functional_interface.IBar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;

public class ImplicitLambdaFunctionalInterfaceMethodRedirectNameOutput {
    public void method1() {
        IBar foo = f -> {
            System.out.println(f);
        };
        foo.bar(new Bar());
    }
}
