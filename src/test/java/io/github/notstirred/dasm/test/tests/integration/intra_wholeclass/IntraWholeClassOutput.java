package io.github.notstirred.dasm.test.tests.integration.intra_wholeclass;

import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Soup;

public class IntraWholeClassOutput {
    CubePos field1 = new CubePos(0, 0, 0);
    static CubePos staticField = new CubePos(0, 0, 0);

    Soup method1() {
        int x = field1.x;
        int y = staticField.y;

        Soup soup = Soup.create();

        int a = soup.b;

        soup.foo2();
        Soup.static_foo2();

        return soup;
    }

    int method3() {
        return Soup.B + Soup.A;
    }
}