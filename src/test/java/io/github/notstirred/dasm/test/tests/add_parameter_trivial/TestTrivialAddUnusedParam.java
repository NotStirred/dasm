package io.github.notstirred.dasm.test.tests.add_parameter_trivial;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.AddUnusedParam;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

@Dasm(TestTrivialAddUnusedParam.Set.class)
public class TestTrivialAddUnusedParam extends BaseMethodTest {
    public TestTrivialAddUnusedParam() {
        super(single(TrivialAddUnusedParamInput.class, TrivialAddUnusedParamOutput.class, TestTrivialAddUnusedParam.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/Object;)Ljava/lang/Object;"))
    public native String method2(String param, @AddUnusedParam Float b);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
