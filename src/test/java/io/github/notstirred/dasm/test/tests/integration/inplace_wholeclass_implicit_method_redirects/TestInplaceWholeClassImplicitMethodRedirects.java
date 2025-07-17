package io.github.notstirred.dasm.test.tests.integration.inplace_wholeclass_implicit_method_redirects;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.test.tests.integration.BaseClassTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for set inheritance
 */
public class TestInplaceWholeClassImplicitMethodRedirects extends BaseClassTest {
    public TestInplaceWholeClassImplicitMethodRedirects() {
        super(single(InplaceWholeClassImplicitMethodRedirectsInput.class, InplaceWholeClassImplicitMethodRedirectsOutput.class, TestInplaceWholeClassImplicitMethodRedirects.class));
    }

    @RedirectSet
    interface Set {
        @TypeRedirect(from = @Ref(Float.class), to = @Ref(String.class))
        abstract class Float_to_String {
        }
    }
}
