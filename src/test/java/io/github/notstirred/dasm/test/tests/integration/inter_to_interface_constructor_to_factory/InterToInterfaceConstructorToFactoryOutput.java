package io.github.notstirred.dasm.test.tests.integration.inter_to_interface_constructor_to_factory;

import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.CubePosInterface;

public class InterToInterfaceConstructorToFactoryOutput {
    public CubePos method1() {
        return new CubePos(5, -10, -1248);
    }

    public CubePosInterface method1out() {
        return CubePosInterface.createCubePos(5, -10, -1248);
    }
}