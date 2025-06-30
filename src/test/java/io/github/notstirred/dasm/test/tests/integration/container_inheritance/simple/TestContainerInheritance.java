package io.github.notstirred.dasm.test.tests.integration.container_inheritance.simple;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.*;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
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
@Dasm(value = TestContainerInheritance.Set.class, target = @Ref(ContainerInheritanceInput.class))
public class TestContainerInheritance extends BaseMethodTest {
    public TestContainerInheritance() {
        super(single(ContainerInheritanceInput.class, ContainerInheritanceOutput.class, TestContainerInheritance.class));
    }

    @TransformFromMethod("method1()V")
    native void method1out();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class Object_to_String_redirects {
            @ConstructorToFactoryRedirect("<init>()V")
            public native static String create();
        }

        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
        abstract class Foo_to_Bar_redirects {
            @FieldRedirect("fooField:I")
            public int barField;

            @MethodRedirect("foo()V")
            public native void bar();

            @FieldToMethodRedirect(value = "fooField2:I", setter = "setBarField2")
            public native int getBarField2();

            @ConstructorToFactoryRedirect("<init>()V")
            public native static Bar create();
        }

        @TypeRedirect(from = @Ref(FooBaz.class), to = @Ref(BarBaz.class))
        abstract class FooBaz_to_BarBaz_redirects extends Foo_to_Bar_redirects {
            @MethodRedirect("foobaz()V")
            public native void barbaz();
        }
    }
}
