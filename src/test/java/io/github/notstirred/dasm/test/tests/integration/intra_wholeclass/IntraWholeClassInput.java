package io.github.notstirred.dasm.test.tests.integration.intra_wholeclass;

import io.github.notstirred.dasm.test.targets.Soup;
import io.github.notstirred.dasm.test.targets.Vec3i;

public class IntraWholeClassInput {
    Vec3i field = new Vec3i(0, 0, 0);
    static Vec3i staticField = new Vec3i(0, 0, 0);

    Soup method1() {
        int x = field.x;
        int y = staticField.y;

        Soup soup = new Soup();

        int a = soup.a;

        soup.foo1();
        Soup.static_foo1();

        return soup;
    }

    int method2() {
        return Soup.A + Soup.B;
    }
}
