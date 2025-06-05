package io.github.notstirred.dasm.test.tests.integration.container_inheritance.interface_to_class;

import io.github.notstirred.dasm.test.targets.AFactory;
import io.github.notstirred.dasm.test.targets.BFactory;
import io.github.notstirred.dasm.test.targets.SubAFactory;
import io.github.notstirred.dasm.test.targets.SubBFactory;

public class InterfaceToClassOutput {
    void method1() {
        BFactory.createB();
    }

    void method2() {
        SubBFactory.createB();
    }

    void method1out() {
        AFactory.createA();
    }

    void method2out() {
        SubAFactory.createA();
    }
}
