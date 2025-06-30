package io.github.notstirred.dasm.test.tests.integration.copy_from_transform;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Type/field/method redirects in a copyFrom method transform.
 * Object -> String
 */
@Dasm(value = TestCopyFromTransform.Set.class, target = @Ref(CopyFromTransformInput.class))
public class TestCopyFromTransform extends BaseMethodTest {
    public TestCopyFromTransform() {
        super(single(CopyFromTransformInput.class, CopyFromTransformOutput.class, TestCopyFromTransform.class));
    }

    @TransformFromMethod("method1(Ljava/lang/Object;)Ljava/lang/Object;")
    public native String method2(String param);

    @TransformFromMethod(value = "methodOnAnotherClass(Ljava/lang/Object;)Ljava/lang/Object;", owner = @Ref(TestCopyFromTransform.class))
    public native String methodOnAnotherClassTransformed(String param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class Object_to_String_redirects {
        }

        @TypeRedirect(from = @Ref(TestCopyFromTransform.class), to = @Ref(CopyFromTransformInput.class))
        abstract class TestCopyFromTransform_to_CopyFromTransformInput_redirects {
        }
    }

    public Object methodOnAnotherClass(Object o) {
        return o;
    }
}
