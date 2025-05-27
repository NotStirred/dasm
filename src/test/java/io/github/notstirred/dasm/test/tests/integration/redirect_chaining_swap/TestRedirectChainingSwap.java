package io.github.notstirred.dasm.test.tests.integration.redirect_chaining_swap;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

@Dasm(TestRedirectChainingSwap.Set.class)
public class TestRedirectChainingSwap extends BaseMethodTest {
    public TestRedirectChainingSwap() {
        super(single(RedirectChainingSwapInput.class, RedirectChainingSwapOutput.class, TestRedirectChainingSwap.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Lio/github/notstirred/dasm/test/targets/Vec3i;)V"))
    public native void method1out(Vec3i param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(CubePos.class))
        abstract class Vec3i_to_CubePos {
        }

        @TypeRedirect(from = @Ref(CubePos.class), to = @Ref(Vec3i.class))
        abstract class CubePos_to_Vec3i {
        }
    }
}
