package io.github.notstirred.dasm.test.tests.integration.set_inheritance;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for set inheritance
 */
@Dasm(value = SetInheritanceDerivedSetA.class, target = @Ref(SetInheritanceInput.class))
public class TestSetInheritance extends BaseMethodTest {
    public TestSetInheritance() {
        super(single(SetInheritanceInput.class, SetInheritanceOutput.class, TestSetInheritance.class));
    }

    @TransformFromMethod("method1()Ljava/lang/Object;")
    native String method1out1();

    @TransformFromMethod(value = "method1()Ljava/lang/Object;", useRedirectSets = SetInheritanceDerivedSetB.class)
    native String method1out2();

}
