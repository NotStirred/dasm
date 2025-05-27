package io.github.notstirred.dasm.test.tests.integration.redirect_chaining_swap;

import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Vec3i;

public class RedirectChainingSwapInput {
    public void method1(Vec3i a) {
        int val = a.x;

        CubePos cubePos = new CubePos(5, 3, 2);

        System.out.printf("thing foo %d\n", val);
    }
}
