package io.github.notstirred.dasm.test.tests.integration.type_and_inter_redirect_constructor_to_factory;

import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.BFactory;

public class TypeAndInterRedirectConstructorToFactoryOutput {
    public A method1() {
        A a = new A();
        a.doAThings();
        return a;
    }

    public B method1out() {
        B a = BFactory.createB();
        a.doBThings();
        return a;
    }
}