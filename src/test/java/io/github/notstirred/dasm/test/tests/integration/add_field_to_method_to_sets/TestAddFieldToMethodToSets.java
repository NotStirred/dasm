package io.github.notstirred.dasm.test.tests.integration.add_field_to_method_to_sets;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for a static {@link AddMethodToSets}
 */
@Dasm(value = TestAddFieldToMethodToSets.Set.class, target = @Ref(AddFieldToMethodToSetsInput.class))
public class TestAddFieldToMethodToSets extends BaseMethodTest {
    public TestAddFieldToMethodToSets() {
        super(single(AddFieldToMethodToSetsInput.class, AddFieldToMethodToSetsOutput.class, TestAddFieldToMethodToSets.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()V"))
    native String method1out();

    @RedirectSet
    public interface Set {
        @InterOwnerContainer(from = @Ref(CubePos.class), to = @Ref(TestAddFieldToMethodToSets.class))
        abstract class CubePos_to_TestAddToSets_redirects {
        }
    }

    static int mask = 123;

    @AddFieldToMethodToSets(containers = Set.CubePos_to_TestAddToSets_redirects.class, setter = "setMask", field = @FieldSig(type = @Ref(int.class), name = "MASK"))
    public static int getMask() {
        return mask;
    }

    public static void setMask(int i) {
        mask = i;
    }
}