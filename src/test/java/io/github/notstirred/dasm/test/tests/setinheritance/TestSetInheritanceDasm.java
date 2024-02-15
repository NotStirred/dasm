package io.github.notstirred.dasm.test.tests.setinheritance;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;

@Dasm(TestSetInheritanceDerivedSet.class)
public class TestSetInheritanceDasm {
    @TransformFromMethod(value = @MethodSig("method1()Ljava/lang/Object;"))
    native String method1out();

}
