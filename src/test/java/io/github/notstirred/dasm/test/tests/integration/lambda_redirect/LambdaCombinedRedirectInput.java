package io.github.notstirred.dasm.test.tests.integration.lambda_redirect;

import io.github.notstirred.dasm.test.targets.Vec3i;

import java.util.stream.IntStream;

public class LambdaCombinedRedirectInput {
    private final int y = 2;
    private final int z = 2;

    public Vec3i[] method1(int count) {
        Vec3i base = new Vec3i(0, y, z);
        return IntStream.range(0, count).mapToObj(val -> new Vec3i(val, base.y, base.z)).toArray(Vec3i[]::new);
    }
}