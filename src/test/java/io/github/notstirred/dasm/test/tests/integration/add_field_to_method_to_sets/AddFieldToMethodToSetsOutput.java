package io.github.notstirred.dasm.test.tests.integration.add_field_to_method_to_sets;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.test.targets.CubePos;

@Dasm(TestAddFieldToMethodToSets.Set.class)
public class AddFieldToMethodToSetsOutput {
    void method1() {
        System.out.println(CubePos.MASK);
        CubePos.MASK = 34;
        System.out.println(CubePos.MASK);
    }

    void method1out() {
        System.out.println(TestAddFieldToMethodToSets.getMask());
        TestAddFieldToMethodToSets.setMask(34);
        System.out.println(TestAddFieldToMethodToSets.getMask());
    }
}