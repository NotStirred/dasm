package io.github.notstirred.dasm.test.tests.integration.add_field_to_method_to_sets;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;

@Dasm(TestAddFieldToMethodToSets.Set.class)
public class AddFieldToMethodToSetsInput {
    void method1() {
        System.out.println(CubePos.MASK);
        CubePos.MASK = 34;
        System.out.println(CubePos.MASK);
    }

    @TransformFromMethod("method1()V")
    native void method1out();
}
