package io.github.notstirred.dasm.test.tests.addtosets;

import io.github.notstirred.dasm.test.targets.CubePos;

public class TestAddToSetsOutput {
    void method1() {
        CubePos.fromLong(0);
    }

    void method1out() {
        TestAddToSetsDasm.testFoo(0);
    }
}