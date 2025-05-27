package io.github.notstirred.dasm.test.tests.integration.combined_field_method_redirects;

import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Vec3i;

public class CombinedFieldMethodRedirectsOutput {
    void method1() {
        Vec3i pos1 = new Vec3i(0);
        System.out.println(pos1.x + ", " + pos1.y + ", " + pos1.z + ", long:" + pos1.asLong());
        Vec3i pos2 = new Vec3i(pos1.getX() + 1, pos1.getY() + 1, pos1.getZ() + 1);
        System.out.println(pos2);
    }

    void method2() {
        Vec3i pos1 = new Vec3i(0);
        System.out.println(pos1.x + ", " + pos1.y + ", " + pos1.z + ", long:" + pos1.asLong());
        Vec3i pos2 = new Vec3i(new Vec3i(0).getX() + 1, new Vec3i(0).getY() + 1, new Vec3i(0).getZ() + 1);
        System.out.println(pos2);
    }

    void method1out() {
        CubePos pos1 = CubePos.from(0);
        System.out.println(pos1.x + ", " + pos1.y + ", " + pos1.z + ", long:" + pos1.asLong());
        CubePos pos2 = new CubePos(pos1.x() + 1, pos1.y() + 1, pos1.z() + 1);
        System.out.println(pos2);
    }

    void method2out() {
        CubePos pos1 = CubePos.from(0);
        System.out.println(pos1.x + ", " + pos1.y + ", " + pos1.z + ", long:" + pos1.asLong());
        CubePos pos2 = new CubePos(CubePos.from(0).x() + 1, CubePos.from(0).y() + 1, CubePos.from(0).z() + 1);
        System.out.println(pos2);
    }
}