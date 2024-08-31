package io.github.notstirred.dasm.test.tests.trivial_type_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * Verify that type redirects work for method parameters and return types.
 * Object -> String
 */
@Dasm(TestTrivialTypeRedirect.Set.class)
public class TestTrivialTypeRedirect extends BaseMethodTest {
    public TestTrivialTypeRedirect() {
        super(single(TrivialTypeRedirectInput.class, TrivialTypeRedirectOutput.class, TestTrivialTypeRedirect.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/Object;)Ljava/lang/Object;"))
    public native String method2(String param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
