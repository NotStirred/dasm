package io.github.notstirred.dasm.test.tests.integration.inter_to_interface_constructor_to_factory;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.CubePosInterface;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Constructor to factory redirects with an additional different type redirect
 */
@Dasm(TestInterToInterfaceConstructorToFactory.Set.class)
public class TestInterToInterfaceConstructorToFactory extends BaseMethodTest {
    public TestInterToInterfaceConstructorToFactory() {
        super(single(InterToInterfaceConstructorToFactoryInput.class, InterToInterfaceConstructorToFactoryOutput.class, TestInterToInterfaceConstructorToFactory.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()Lio/github/notstirred/dasm/test/targets/CubePos;"))
    public native String method1out(String param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(CubePos.class), to = @Ref(CubePosInterface.class))
        interface CubePos_to_CubePosInterface_redirects {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {@Ref(int.class), @Ref(int.class), @Ref(int.class)}))
            static CubePosInterface createCubePos(int x, int y, int z) {
                throw new IllegalStateException("Not possible.");
            }
        }
    }
}
