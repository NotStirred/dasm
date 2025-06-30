package io.github.notstirred.dasm.test.tests.integration.missing_dup_after_new;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Attempt to create a situation where there is no DUP after a NEW instruction for testing {@link io.github.notstirred.dasm.transformer.ConstructorToFactoryBufferingVisitor ConstructorToFactoryBufferingVisitor}'s consumedDup checks.
 * <p>
 * Currently, this <u>does not work</u> as javac always produces a DUP instruction.
 */
@Dasm(value = TestMissingDupAfterNew.Set.class, target = @Ref(MissingDupAfterNewInput.class))
public class TestMissingDupAfterNew extends BaseMethodTest {
    public TestMissingDupAfterNew() {
        super(single(MissingDupAfterNewInput.class, MissingDupAfterNewOutput.class, TestMissingDupAfterNew.class));
    }

    @TransformFromMethod("method1()V")
    public native void method1out();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(CubePos.class))
        abstract class A {
        }
    }
}
