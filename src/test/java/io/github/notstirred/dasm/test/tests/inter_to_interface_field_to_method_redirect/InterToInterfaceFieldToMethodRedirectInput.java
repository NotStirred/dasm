package io.github.notstirred.dasm.test.tests.inter_to_interface_field_to_method_redirect;

import io.github.notstirred.dasm.test.targets.Vec3i;

public class InterToInterfaceFieldToMethodRedirectInput {
    public void method1(Vec3i a) {
        int val = a.x;
        System.out.printf("thing foo %d\n", val);
        a.x = val + 1;
    }
}