package io.github.notstirred.dasm.test.tests.missing_dup_after_new;

import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Vec3i;

public class MissingDupAfterNewOutput {
    public void method1() {
        new Vec3i(0, 0, 0);
    }

    public void method1out() {
        new CubePos(0, 0, 0);
    }
}