package io.github.notstirred.dasm.test.tests.integration.inter_field_to_method_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldToMethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

@Dasm(value = TestInterFieldToMethodRedirect.Set.class, target = @Ref(InterFieldToMethodRedirectInput.class))
public class TestInterFieldToMethodRedirect extends BaseMethodTest {
    public TestInterFieldToMethodRedirect() {
        super(single(InterFieldToMethodRedirectInput.class, InterFieldToMethodRedirectOutput.class, TestInterFieldToMethodRedirect.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Lio/github/notstirred/dasm/test/targets/Vec3i;)V"))
    public native void method1out(Vec3i param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(CubePos.class))
        abstract class A {
            @FieldToMethodRedirect(value = @FieldSig(type = @Ref(int.class), name = "x"), setter = "setX")
            public native int x();
        }
    }
}
