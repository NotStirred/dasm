package io.github.notstirred.dasm.test.tests.integration.redirect_chaining_three_types;

import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.C;

public class RedirectChainingThreeTypesOutput {
    public void method1(A a) {
        A a1 = new A();
        B b1 = new B();
        C c1 = new C();
    }

    public void method1out(B a) {
        B a1 = new B();
        C b1 = new C();
        C c1 = new C();
    }
}