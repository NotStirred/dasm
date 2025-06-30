package io.github.notstirred.dasm.test.tests.integration.add_to_sets;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for a static {@link AddMethodToSets}
 */
@Dasm(value = TestAddToSets.Set.class)
public class TestAddToSets extends BaseMethodTest {
    public TestAddToSets() {
        super(single(AddToSetsInput.class, AddToSetsOutput.class, TestAddToSets.class));
    }

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }

        @InterOwnerContainer(from = @Ref(CubePos.class), to = @Ref(TestAddToSets.class))
        abstract class CubePos_to_TestAddToSets_redirects {
        }
    }

    @AddFieldToSets(containers = Set.CubePos_to_TestAddToSets_redirects.class, field = "MASK:I")
    public static int TEST_MASK = 123;

    @AddMethodToSets(containers = Set.CubePos_to_TestAddToSets_redirects.class, method = "from(J)Lio/github/notstirred/dasm/test/targets/CubePos;")
    public static CubePos testFoo(long l) {
        return null;
    }
}