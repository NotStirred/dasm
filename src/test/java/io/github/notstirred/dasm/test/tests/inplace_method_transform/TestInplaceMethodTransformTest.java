package io.github.notstirred.dasm.test.tests.inplace_method_transform;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformMethod;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * TODO
 */
@Dasm(TestInplaceMethodTransformTest.Set.class)
public class TestInplaceMethodTransformTest extends BaseMethodTest {
    public TestInplaceMethodTransformTest() {
        super(single(InplaceMethodTransformTestInput.class, InplaceMethodTransformTestOutput.class, TestInplaceMethodTransformTest.class));
    }

    @TransformMethod(@MethodSig(name = "method1", args = { @Ref(Float.class) }, ret = @Ref(void.class)))
    public native void method1(String a);

    @RedirectSet
    interface Set {
        @TypeRedirect(from = @Ref(Float.class), to = @Ref(String.class))
        class Float_to_String_redirects {}
    }
}
