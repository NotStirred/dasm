package io.github.notstirred.dasm.test.tests.integration.add_field_to_method_to_sets;

import io.github.notstirred.dasm.test.targets.CubePos;

public class AddFieldToMethodToSetsInput {
    void method1() {
        System.out.println(CubePos.MASK);
        CubePos.MASK = 34;
        System.out.println(CubePos.MASK);
    }
}
