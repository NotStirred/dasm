package io.github.notstirred.dasm.test.tests.integration.inter_noargs_constructor_to_factory;

import io.github.notstirred.dasm.test.targets.NewSoup;
import io.github.notstirred.dasm.test.targets.Soup;

public class InterNoArgsConstructorToFactoryOutput {
    public Soup method1() {
        return new Soup();
    }

    public NewSoup method1out() {
        return NewSoup.create();
    }
}