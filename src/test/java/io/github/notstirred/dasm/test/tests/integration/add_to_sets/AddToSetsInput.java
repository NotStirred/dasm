package io.github.notstirred.dasm.test.tests.integration.add_to_sets;

import io.github.notstirred.dasm.test.targets.CubePos;

public class AddToSetsInput {
    void method1() {
        CubePos.from(0);
        System.out.println(CubePos.MASK);
    }
}
