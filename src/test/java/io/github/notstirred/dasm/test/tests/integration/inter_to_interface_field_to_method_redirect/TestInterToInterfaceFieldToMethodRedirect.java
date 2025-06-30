package io.github.notstirred.dasm.test.tests.integration.inter_to_interface_field_to_method_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldToMethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePosInterface;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

@Dasm(value = TestInterToInterfaceFieldToMethodRedirect.Set.class, target = @Ref(InterToInterfaceFieldToMethodRedirectInput.class))
public class TestInterToInterfaceFieldToMethodRedirect extends BaseMethodTest {
    public TestInterToInterfaceFieldToMethodRedirect() {
        super(single(InterToInterfaceFieldToMethodRedirectInput.class, InterToInterfaceFieldToMethodRedirectOutput.class, TestInterToInterfaceFieldToMethodRedirect.class));
    }

    @TransformFromMethod("method1(Lio/github/notstirred/dasm/test/targets/Vec3i;)V")
    public native void method1out(Vec3i param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(CubePosInterface.class))
        interface A {
            @FieldToMethodRedirect(value = "x:I", setter = "setX")
            int x();
        }
    }
}
