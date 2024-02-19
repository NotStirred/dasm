package io.github.notstirred.dasm.test.tests.combined_field_method_redirects;


import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * A trivial test for a static {@link AddMethodToSets}
 */
@Dasm(io.github.notstirred.dasm.test.tests.add_to_sets.TestAddToSets.Set.class)
public class TestCombinedFieldMethodRedirects extends BaseMethodTest {
    public TestCombinedFieldMethodRedirects() {
        super(single(CombinedFieldMethodRedirectsInput.class, CombinedFieldMethodRedirectsOutput.class, TestCombinedFieldMethodRedirects.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()V"))
    native String method1out();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A { }
    }

    @AddMethodToSets(owner = @Ref(CubePos.class), method = @MethodSig(name = "fromLong", ret = @Ref(CubePos.class), args = { @Ref(long.class) }), sets = io.github.notstirred.dasm.test.tests.add_to_sets.TestAddToSets.Set.class)
    public static CubePos testFoo(long l) {
        return null;
    }
}
