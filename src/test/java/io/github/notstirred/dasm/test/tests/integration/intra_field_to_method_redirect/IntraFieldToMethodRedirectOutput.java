package io.github.notstirred.dasm.test.tests.integration.intra_field_to_method_redirect;

import io.github.notstirred.dasm.test.targets.Vec3i;

public class IntraFieldToMethodRedirectOutput {
    public void method1(Vec3i a) {
        int val = a.x;
        System.out.printf("thing foo %d\n", val);
        a.x = val + 1;
    }

    public void method1out(Vec3i a) {
        int val = a.getX();
        System.out.printf("thing foo %d\n", val);
        a.setX(val + 1);
    }
}