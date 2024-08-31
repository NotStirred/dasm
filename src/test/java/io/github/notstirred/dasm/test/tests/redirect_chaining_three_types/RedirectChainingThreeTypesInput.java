package io.github.notstirred.dasm.test.tests.redirect_chaining_three_types;

import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.C;

public class RedirectChainingThreeTypesInput {
    public void method1(A a) {
        A a1 = new A();
        B b1 = new B();
        C c1 = new C();
    }
}
