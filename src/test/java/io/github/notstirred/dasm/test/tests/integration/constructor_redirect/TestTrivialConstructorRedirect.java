package io.github.notstirred.dasm.test.tests.integration.constructor_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Verify that constructors are properly redirected.
 * new Object() -> new String()
 */
@Dasm(TestTrivialConstructorRedirect.Set.class)
public class TestTrivialConstructorRedirect extends BaseMethodTest {
    public TestTrivialConstructorRedirect() {
        super(single(TrivialConstructorRedirectInput.class, TrivialConstructorRedirectOutput.class, TestTrivialConstructorRedirect.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()Ljava/lang/Object;"))
    native String method2();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A { }
    }
}