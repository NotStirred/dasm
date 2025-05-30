package io.github.notstirred.dasm.test.tests.integration.add_parameter_no_code_case;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.AddUnusedParam;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.C;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

@Dasm(value = TestAddParameterAbstract.Set.class, target = @Ref(AddParameterAbstractInput.class))
public class TestAddParameterAbstract extends BaseMethodTest {
    public TestAddParameterAbstract() {
        super(single(AddParameterAbstractInput.class, AddParameterAbstractOutput.class, TestAddParameterAbstract.class));
    }

    // Error case for https://discord.com/channels/316679487955927050/317206370359443458/1343090582087532544
    // An abstract method without an added parameter.
    @TransformFromMethod(value = @MethodSig("method1()V"))
    public native void method1out1();

    @TransformFromMethod(value = @MethodSig("method2(Lio/github/notstirred/dasm/test/targets/A;)V"))
    public native void method2out(B foo, @AddUnusedParam C foo2);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(A.class), to = @Ref(B.class))
        abstract class A_to_B {
        }
    }
}
