package io.github.notstirred.dasm.test.tests.integration.add_parameter_no_code_case;

import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.C;

public abstract class AddParameterAbstractOutput {
    public abstract void method1();

    public abstract void method1out();

    public abstract void method2(A foo);

    public abstract void method2out(B foo, C foo2);
}