package io.github.notstirred.dasm.test.tests.integration.type_and_inter_redirect_constructor_to_factory;

import io.github.notstirred.dasm.test.targets.A;

public class TypeAndInterRedirectConstructorToFactoryInput {
    public A method1() {
        A a = new A();
        a.doAThings();
        return a;
    }
}