package io.github.notstirred.dasm.test.tests.integration.inherited_transforms.simple;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.*;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.BarBaz;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.targets.inherited_transforms.FooBaz;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * //TODO
 */
@Dasm(value = TestInheritedTransforms.Set.class, target = @Ref(InheritedTransformsInput.class))
public class TestInheritedTransforms extends BaseMethodTest {
    public TestInheritedTransforms() {
        super(single(InheritedTransformsInput.class, InheritedTransformsOutput.class, TestInheritedTransforms.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()V"))
    native void method1out();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class Object_to_String_redirects {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {}))
            public native static String create();
        }

        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
        abstract class Foo_to_Bar_redirects {
            @FieldRedirect(@FieldSig(type = @Ref(int.class), name = "fooField"))
            public int barField;

            @FieldRedirect(@FieldSig(type = @Ref(Foo.class), name = "instance"))
            public static Bar instance;

            @MethodRedirect(@MethodSig(name = "foo", ret = @Ref(void.class)))
            public native void bar();

            @FieldToMethodRedirect(value = @FieldSig(type = @Ref(int.class), name = "fooField2"), setter = "setBarField2")
            public native int getBarField2();

            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {}))
            public native static Bar create();
        }

        @TypeRedirect(from = @Ref(FooBaz.class), to = @Ref(BarBaz.class))
        abstract class FooBaz_to_BarBaz_redirects extends Foo_to_Bar_redirects {
        }
    }
}
