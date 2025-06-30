package io.github.notstirred.dasm.test.tests.integration.combined_field_method_redirects;


import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for a static {@link AddMethodToSets}
 */
@Dasm(value = TestCombinedFieldMethodRedirects.Set.class)
public class TestCombinedFieldMethodRedirects extends BaseMethodTest {
    public TestCombinedFieldMethodRedirects() {
        super(single(CombinedFieldMethodRedirectsInput.class, CombinedFieldMethodRedirectsOutput.class, TestCombinedFieldMethodRedirects.class));
    }

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(CubePos.class))
        abstract class A {
            @MethodRedirect("getX()I")
            native int x();

            @MethodRedirect("getY()I")
            native int y();

            @MethodRedirect("getZ()I")
            native int z();

            @ConstructorToFactoryRedirect("<init>(J)V")
            native CubePos from();
        }

        @InterOwnerContainer(from = @Ref(Vec3i.class), to = @Ref(TestCombinedFieldMethodRedirects.class))
        abstract class B {
        }
    }

    @AddMethodToSets(containers = Set.B.class, method = "from(J)Lio/github/notstirred/dasm/test/targets/Vec3i;")
    public static CubePos from(long l) {
        return CubePos.from(l);
    }
}
