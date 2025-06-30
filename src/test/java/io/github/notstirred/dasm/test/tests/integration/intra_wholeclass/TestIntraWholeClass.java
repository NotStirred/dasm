package io.github.notstirred.dasm.test.tests.integration.intra_wholeclass;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.targets.Soup;
import io.github.notstirred.dasm.test.targets.Vec3i;
import io.github.notstirred.dasm.test.tests.integration.BaseClassTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for set inheritance
 */
@TransformFromClass(value = @Ref(IntraWholeClassInput.class), sets = TestIntraWholeClass.Set.class)
public class TestIntraWholeClass extends BaseClassTest {
    public TestIntraWholeClass() {
        super(single(IntraWholeClassInput.class, IntraWholeClassOutput.class, TestIntraWholeClass.class));
    }

    @RedirectSet
    interface Set {
        @IntraOwnerContainer(@Ref(IntraWholeClassInput.class))
        abstract class InputRedirects {
            @FieldRedirect("field:Lio/github/notstirred/dasm/test/targets/Vec3i;")
            float field1;

            @MethodRedirect("method2()I")
            native Soup method3();
        }

        @IntraOwnerContainer(@Ref(Soup.class))
        abstract class SoupToStringRedirects {
            @FieldRedirect("a:I")
            float b;

            @FieldRedirect("A:I")
            static int B;

            @FieldRedirect("B:I")
            static int A;

            @MethodRedirect("foo1()V")
            native void foo2();

            @MethodRedirect("static_foo1()V")
            native static void static_foo2();

            @ConstructorToFactoryRedirect("<init>()V")
            native static Soup create();
        }

        @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(CubePos.class))
        abstract class Vec3i_to_CubePos_redirects {
        }
    }
}
