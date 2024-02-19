package io.github.notstirred.dasm.test.tests.combined_field_method_redirects;

import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.tests.add_to_sets.TestAddToSets;

public class CombinedFieldMethodRedirectsOutput {
    void method1() {
        CubePos.fromLong(0);
    }

    void method1out() {
        TestAddToSets.testFoo(0);
    }
}