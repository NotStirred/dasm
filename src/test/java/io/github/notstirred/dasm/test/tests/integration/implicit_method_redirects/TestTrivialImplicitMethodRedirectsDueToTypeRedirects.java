package io.github.notstirred.dasm.test.tests.integration.implicit_method_redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Verify that type redirects implicitly do self method redirects.
 * Object#hashCode() -> String#hashCode()
 */
@Dasm(value = TestTrivialImplicitMethodRedirectsDueToTypeRedirects.Set.class, target = @Ref(TrivialImplicitMethodRedirectsDueToTypeRedirectsInput.class))
public class TestTrivialImplicitMethodRedirectsDueToTypeRedirects extends BaseMethodTest {
    public TestTrivialImplicitMethodRedirectsDueToTypeRedirects() {
        super(single(
                TrivialImplicitMethodRedirectsDueToTypeRedirectsInput.class, TrivialImplicitMethodRedirectsDueToTypeRedirectsOutput.class,
                TestTrivialImplicitMethodRedirectsDueToTypeRedirects.class
        ));
    }

    @TransformFromMethod("method1(Ljava/lang/Float;)V")
    public native String method1(String param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Float.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
