package io.github.notstirred.dasm.test.targets.functional_interface;

import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;

@FunctionalInterface
public interface IBar {
    void bar(Bar bar);
}
