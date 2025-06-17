package io.github.notstirred.dasm.test.tests.integration.combined_field_method_redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.Vec3i;

@Dasm(value = TestCombinedFieldMethodRedirects.Set.class)
public class CombinedFieldMethodRedirectsInput {
    void method1() {
        Vec3i pos1 = new Vec3i(0);
        System.out.println(pos1.x + ", " + pos1.y + ", " + pos1.z + ", long:" + pos1.asLong());
        Vec3i pos2 = new Vec3i(pos1.getX() + 1, pos1.getY() + 1, pos1.getZ() + 1);
        System.out.println(pos2);
        Vec3i.from(pos2.asLong());
    }

    void method2() {
        Vec3i pos1 = new Vec3i(0);
        System.out.println(pos1.x + ", " + pos1.y + ", " + pos1.z + ", long:" + pos1.asLong());
        Vec3i pos2 = new Vec3i(new Vec3i(0).getX() + 1, new Vec3i(0).getY() + 1, new Vec3i(0).getZ() + 1);
        System.out.println(pos2);
    }

    @TransformFromMethod(value = @MethodSig("method1()V"))
    native void method1out();

    @TransformFromMethod(value = @MethodSig("method2()V"))
    native void method2out();

}
