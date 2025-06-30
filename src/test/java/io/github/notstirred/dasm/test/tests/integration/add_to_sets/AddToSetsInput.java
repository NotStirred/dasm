package io.github.notstirred.dasm.test.tests.integration.add_to_sets;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;

@Dasm(TestAddToSets.Set.class)
public class AddToSetsInput {
    void method1() {
        CubePos.from(0);
        System.out.println(CubePos.MASK);
    }

    @TransformFromMethod("method1()V")
    native void method1out();
}
