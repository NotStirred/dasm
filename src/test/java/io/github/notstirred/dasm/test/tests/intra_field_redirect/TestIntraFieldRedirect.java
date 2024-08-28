package io.github.notstirred.dasm.test.tests.intra_field_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.Soup;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * Verify that method redirects work
 * String#hashCode() -> String#length()
 */
@Dasm(TestIntraFieldRedirect.T3Set.class)
public class TestIntraFieldRedirect extends BaseMethodTest {
    public TestIntraFieldRedirect() {
        super(single(IntraFieldRedirectInput.class, IntraFieldRedirectOutput.class, TestIntraFieldRedirect.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Lio/github/notstirred/dasm/test/targets/Vec3i;)V"))
    native void method1out(Vec3i param);

    @TransformFromMethod(value = @MethodSig("method2(I)V"))
    native void method2out(int val);

    @RedirectSet
    public interface T3Set {
        @IntraOwnerContainer(owner = @Ref(Vec3i.class))
        abstract class Vec3i_NONSTATIC {
            @FieldRedirect(@FieldSig(type = @Ref(int.class), name = "x"))
            public int y;
        }

        @IntraOwnerContainer(owner = @Ref(Soup.class))
        abstract class Soup_STATIC {
            @FieldRedirect(@FieldSig(type = @Ref(int.class), name = "A"))
            public static int B;
        }
    }
}