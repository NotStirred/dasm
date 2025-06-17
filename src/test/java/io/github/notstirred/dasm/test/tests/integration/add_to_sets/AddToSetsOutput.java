package io.github.notstirred.dasm.test.tests.integration.add_to_sets;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.test.targets.CubePos;

@Dasm(TestAddToSets.Set.class)
public class AddToSetsOutput {
    void method1() {
        CubePos.from(0);
        System.out.println(CubePos.MASK);
    }

    void method1out() {
        TestAddToSets.testFoo(0);
        System.out.println(TestAddToSets.TEST_MASK);
    }
}