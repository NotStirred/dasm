package io.github.notstirred.dasm.test.tests.intra_onearg_constructor_to_factory;

import io.github.notstirred.dasm.test.targets.Soup;

public class IntraOneArgConstructorToFactoryOutput {
    public Soup method1() {
        return new Soup(5);
    }

    public Soup method1out() {
        return Soup.create(5);
    }
}