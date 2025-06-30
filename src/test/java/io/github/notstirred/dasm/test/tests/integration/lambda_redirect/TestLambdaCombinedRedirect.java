package io.github.notstirred.dasm.test.tests.integration.lambda_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldToMethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformMethod;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

@Dasm(value = TestLambdaCombinedRedirect.Set.class, target = @Ref(LambdaCombinedRedirectInput.class))
public class TestLambdaCombinedRedirect extends BaseMethodTest {
    public TestLambdaCombinedRedirect() {
        super(single(LambdaCombinedRedirectInput.class, LambdaCombinedRedirectOutput.class, TestLambdaCombinedRedirect.class));
    }

    @TransformMethod("method1(I)[Lio/github/notstirred/dasm/test/targets/Vec3i;")
    public native CubePos[] method1(int count);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(CubePos.class))
        abstract class Vec3i_to_CubePos_redirects {
            @FieldToMethodRedirect("y:I")
            public native int y();

            @FieldToMethodRedirect("z:I")
            public native int z();

            @ConstructorToFactoryRedirect("<init>(III)V")
            public native CubePos of(int x, int y, int z);
        }
    }
}
