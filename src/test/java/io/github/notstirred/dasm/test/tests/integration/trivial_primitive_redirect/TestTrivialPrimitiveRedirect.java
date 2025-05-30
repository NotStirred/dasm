package io.github.notstirred.dasm.test.tests.integration.trivial_primitive_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Verify that type redirects work for method parameters and return types.
 * Object -> String
 */
@Dasm(value = TestTrivialPrimitiveRedirect.Set.class, target = @Ref(TrivialPrimitiveRedirectInput.class))
public class TestTrivialPrimitiveRedirect extends BaseMethodTest {
    public TestTrivialPrimitiveRedirect() {
        super(single(TrivialPrimitiveRedirectInput.class, TrivialPrimitiveRedirectOutput.class, TestTrivialPrimitiveRedirect.class));
    }

    @TransformFromMethod(value = @MethodSig("method1([I)[I"))
    public native float[] method2(float[] a);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(int.class), to = @Ref(float.class))
        abstract class A {
        }
    }
}
