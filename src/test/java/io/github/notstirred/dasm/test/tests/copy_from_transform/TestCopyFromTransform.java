package io.github.notstirred.dasm.test.tests.copy_from_transform;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * Type/field/method redirects in a copyFrom method transform.
 * Object -> String
 */
@Dasm(TestCopyFromTransform.T5Set.class)
public class TestCopyFromTransform extends BaseMethodTest {
    public TestCopyFromTransform() {
        super(single(CopyFromTransformInput.class, CopyFromTransformOutput.class, TestCopyFromTransform.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/Object;)Ljava/lang/Object;"))
    public native String method2(String param);

    @TransformFromMethod(value = @MethodSig("methodOnAnotherClass(Ljava/lang/Object;)Ljava/lang/Object;"), copyFrom = @Ref(TestCopyFromTransform.class))
    public native String methodOnAnotherClassTransformed(String param);

    @RedirectSet
    public interface T5Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A { }

        @TypeRedirect(from = @Ref(TestCopyFromTransform.class), to = @Ref(CopyFromTransformInput.class))
        abstract class T5DasmToT5InputRedirect { }
    }

    public Object methodOnAnotherClass(Object o) {
        return o;
    }
}
