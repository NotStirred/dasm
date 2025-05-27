package io.github.notstirred.dasm.test.tests.integration.intra_wholeclass;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
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
        @IntraOwnerContainer(owner = @Ref(IntraWholeClassInput.class))
        abstract class InputRedirects {
            @FieldRedirect(@FieldSig(type = @Ref(Vec3i.class), name = "field"))
            float field1;

            @MethodRedirect(@MethodSig(name = "method2", args = {}, ret = @Ref(int.class)))
            native Soup method3();
        }

        @IntraOwnerContainer(owner = @Ref(Soup.class))
        abstract class SoupToStringRedirects {
            @FieldRedirect(@FieldSig(type = @Ref(int.class), name = "a"))
            float b;

            @FieldRedirect(@FieldSig(type = @Ref(int.class), name = "A"))
            static int B;

            @FieldRedirect(@FieldSig(type = @Ref(int.class), name = "B"))
            static int A;

            @MethodRedirect(@MethodSig(name = "foo1", ret = @Ref(void.class), args = {}))
            native void foo2();

            @MethodRedirect(@MethodSig(name = "static_foo1", ret = @Ref(void.class), args = {}))
            native static void static_foo2();

            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {}))
            native static Soup create();
        }

        @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(CubePos.class))
        abstract class Vec3i_to_CubePos_redirects {
        }
    }
}
