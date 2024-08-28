package io.github.notstirred.dasm.test.tests.intra_field_redirect;

import io.github.notstirred.dasm.test.targets.Soup;
import io.github.notstirred.dasm.test.targets.Vec3i;

public class IntraFieldRedirectInput {
    public void method1(Vec3i a) {
        int val = a.x;
        System.out.printf("thing foo %d\n", val);
    }

    public void method2(int val) {
        int a = Soup.A;
        Soup.A = a;
        System.out.printf("valsoup %d\n", val);
    }
}
