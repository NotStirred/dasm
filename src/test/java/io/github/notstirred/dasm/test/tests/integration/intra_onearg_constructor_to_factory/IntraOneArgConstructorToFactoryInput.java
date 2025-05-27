package io.github.notstirred.dasm.test.tests.integration.intra_onearg_constructor_to_factory;

import io.github.notstirred.dasm.test.targets.Soup;

public class IntraOneArgConstructorToFactoryInput {
    public Soup method1() {
        return new Soup(5);
    }
}