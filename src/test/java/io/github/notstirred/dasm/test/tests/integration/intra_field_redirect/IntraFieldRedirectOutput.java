package io.github.notstirred.dasm.test.tests.integration.intra_field_redirect;

import io.github.notstirred.dasm.test.targets.Soup;
import io.github.notstirred.dasm.test.targets.Vec3i;

public class IntraFieldRedirectOutput {
    public void method1(Vec3i a) {
        int val = a.x;
        System.out.printf("thing foo %d\n", val);
    }

    public void method2(int val) {
        int a = Soup.A;
        Soup.A = a;
        System.out.printf("valsoup %d\n", val);
    }

    public void method1out(Vec3i a) {
        int val = a.y;
        System.out.printf("thing foo %d\n", val);
    }

    public void method2out(int val) {
        int a = Soup.B;
        Soup.B = a;
        System.out.printf("valsoup %d\n", val);
    }
}