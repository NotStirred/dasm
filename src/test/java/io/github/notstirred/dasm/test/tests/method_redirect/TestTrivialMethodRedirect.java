package io.github.notstirred.dasm.test.tests.method_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * Verify that method redirects work
 * String#hashCode() -> String#length()
 */
@Dasm(TestTrivialMethodRedirect.T3Set.class)
public class TestTrivialMethodRedirect extends BaseMethodTest {
    public TestTrivialMethodRedirect() {
        super(single(TrivialMethodRedirectInput.class, TrivialMethodRedirectOutput.class, TestTrivialMethodRedirect.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/String;)V"))
    native String method2(String param);

    @RedirectSet
    public interface T3Set {
        @IntraOwnerContainer(owner = @Ref(String.class))
        abstract class A {
            @MethodRedirect(@MethodSig(ret = @Ref(int.class), name = "hashCode", args = { }))
            public native int length();
        }
    }
}
