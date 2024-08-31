package io.github.notstirred.dasm.test.tests.intra_method_redirect;

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
@Dasm(TestIntraMethodRedirect.Set.class)
public class TestIntraMethodRedirect extends BaseMethodTest {
    public TestIntraMethodRedirect() {
        super(single(IntraMethodRedirectInput.class, IntraMethodRedirectOutput.class, TestIntraMethodRedirect.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/String;)V"))
    native void method1out(String param);

    @RedirectSet
    public interface Set {
        @IntraOwnerContainer(owner = @Ref(String.class))
        abstract class A {
            @MethodRedirect(@MethodSig(ret = @Ref(int.class), name = "hashCode", args = { }))
            public native int length();
        }
    }
}
