package io.github.notstirred.dasm.test.tests.integration.intra_field_to_method_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldToMethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

@Dasm(value = TestIntraFieldToMethodRedirect.Set.class, target = @Ref(IntraFieldToMethodRedirectInput.class))
public class TestIntraFieldToMethodRedirect extends BaseMethodTest {
    public TestIntraFieldToMethodRedirect() {
        super(single(IntraFieldToMethodRedirectInput.class, IntraFieldToMethodRedirectOutput.class, TestIntraFieldToMethodRedirect.class));
    }

    @TransformFromMethod("method1(Lio/github/notstirred/dasm/test/targets/Vec3i;)V")
    public native void method1out(Vec3i param);

    @RedirectSet
    public interface Set {
        @IntraOwnerContainer(@Ref(Vec3i.class))
        abstract class A {
            @FieldToMethodRedirect(value = "x:I", setter = "setX")
            public native int getX();
        }
    }
}
