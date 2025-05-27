package io.github.notstirred.dasm.test.tests.integration.intra_noargs_constructor_to_factory;

import io.github.notstirred.dasm.test.targets.Soup;

public class IntraNoArgsConstructorToFactoryOutput {
    public Soup method1() {
        return new Soup();
    }

    public Soup method1out() {
        return Soup.create();
    }
}