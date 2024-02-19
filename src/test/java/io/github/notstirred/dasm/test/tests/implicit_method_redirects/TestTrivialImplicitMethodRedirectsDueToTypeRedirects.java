package io.github.notstirred.dasm.test.tests.implicit_method_redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * Verify that type redirects implicitly do self method redirects.
 * Object#hashCode() -> String#hashCode()
 */
@Dasm(TestTrivialImplicitMethodRedirectsDueToTypeRedirects.T2Set.class)
public class TestTrivialImplicitMethodRedirectsDueToTypeRedirects extends BaseMethodTest {
    public TestTrivialImplicitMethodRedirectsDueToTypeRedirects() {
        super(single(
                TrivialImplicitMethodRedirectsDueToTypeRedirectsInput.class, TrivialImplicitMethodRedirectsDueToTypeRedirectsOutput.class,
                TestTrivialImplicitMethodRedirectsDueToTypeRedirects.class
        ));
    }

    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/Float;)V"))
    native String method1(String param);

    @RedirectSet
    public interface T2Set {
        @TypeRedirect(from = @Ref(Float.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
