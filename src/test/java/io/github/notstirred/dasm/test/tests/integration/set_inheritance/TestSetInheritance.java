package io.github.notstirred.dasm.test.tests.integration.set_inheritance;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for set inheritance
 */
@Dasm(SetInheritanceDerivedSetA.class)
public class TestSetInheritance extends BaseMethodTest {
    public TestSetInheritance() {
        super(single(SetInheritanceInput.class, SetInheritanceOutput.class, TestSetInheritance.class));
    }
    @TransformFromMethod(value = @MethodSig("method1()Ljava/lang/Object;"))
    native String method1out1();

    @TransformFromMethod(value = @MethodSig("method1()Ljava/lang/Object;"), useRedirectSets = SetInheritanceDerivedSetB.class)
    native String method1out2();

}
