package io.github.notstirred.dasm.test.tests.integration.container_inheritance.interface_to_class;

import io.github.notstirred.dasm.test.targets.BFactory;
import io.github.notstirred.dasm.test.targets.SubBFactory;

public class InterfaceToClassInput {
    void method1() {
        BFactory.createB();
    }

    void method2() {
        SubBFactory.createB();
    }
}
