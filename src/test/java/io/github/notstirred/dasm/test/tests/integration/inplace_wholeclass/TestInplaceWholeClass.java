package io.github.notstirred.dasm.test.tests.integration.inplace_wholeclass;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformClass;
import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.tests.integration.BaseClassTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for set inheritance
 */
@TransformClass(sets = TestInplaceWholeClass.Set.class)
public class TestInplaceWholeClass extends BaseClassTest {
    public TestInplaceWholeClass() {
        super(single(InplaceWholeClassInput.class, InplaceWholeClassOutput.class, TestInplaceWholeClass.class));
    }

    @RedirectSet
    interface Set {
        @TypeRedirect(from = @Ref(A.class), to = @Ref(B.class))
        abstract class A_to_B {
            @MethodRedirect("doAThings()V")
            native void doBThings();
        }

        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
        abstract class Foo_to_Bar {
            @FieldRedirect("fooField:I")
            int barField;

            @MethodRedirect("foo()V")
            native void bar();
        }
    }
}
