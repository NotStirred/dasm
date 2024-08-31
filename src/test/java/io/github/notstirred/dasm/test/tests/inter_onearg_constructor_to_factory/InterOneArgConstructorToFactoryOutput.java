package io.github.notstirred.dasm.test.tests.inter_onearg_constructor_to_factory;

import io.github.notstirred.dasm.test.targets.NewSoup;
import io.github.notstirred.dasm.test.targets.Soup;

public class InterOneArgConstructorToFactoryOutput {
    public Soup method1() {
        return new Soup(5);
    }

    public NewSoup method1out() {
        return NewSoup.create(5);
    }
}