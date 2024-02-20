package io.github.notstirred.dasm.test.tests.add_to_sets;

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
@Dasm(TestAddToSets.Set.class)
public class TestAddToSets extends BaseMethodTest {
    public TestAddToSets() {
        super(single(AddToSetsInput.class, AddToSetsOutput.class, TestAddToSets.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()V"))
    native String method1out();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A { }
    }

    @AddMethodToSets(owner = @Ref(CubePos.class), method = @MethodSig(name = "from", ret = @Ref(CubePos.class), args = { @Ref(long.class) }), sets = Set.class)
    public static CubePos testFoo(long l) {
        return null;
    }
}