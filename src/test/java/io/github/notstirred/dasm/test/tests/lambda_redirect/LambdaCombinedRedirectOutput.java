package io.github.notstirred.dasm.test.tests.lambda_redirect;

import io.github.notstirred.dasm.test.targets.CubePos;

import java.util.stream.IntStream;

public class LambdaCombinedRedirectOutput {
    private final int y = 2;
    private final int z = 2;

    public CubePos[] method1(int count) {
        CubePos base = CubePos.of(0, y, z);
        return IntStream.range(0, count).mapToObj(val -> CubePos.of(val, base.y(), base.z())).toArray(CubePos[]::new);
    }
}