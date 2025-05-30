package io.github.notstirred.dasm.test.tests.integration.inherited_sets_override_priority;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
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
 * Verify that inherited redirects are overridden correctly.
 * Essentially the later sets in the inheritance tree have the last say.
 */
@Dasm(TestInheritedSetsOverridePriority.HasOverride.class)
public class TestInheritedSetsOverridePriority extends BaseMethodTest {
    public TestInheritedSetsOverridePriority() {
        super(single(InheritedSetsOverridePriorityInput.class, InheritedSetsOverridePriorityOutput.class, TestInheritedSetsOverridePriority.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()V"), useRedirectSets = HasOverride.class)
    public native void method1out1();

    @TransformFromMethod(value = @MethodSig("method1()V"), useRedirectSets = NoOverride.class)
    public native void method1out2();

    @RedirectSet
    public interface HasOverride extends B, C {
        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(BarBaz.class))
        abstract class Foo_to_BarBaz_redirects {
            @MethodRedirect(@MethodSig("foo()V"))
            public native void barbaz();
        }
    }

    @RedirectSet
    public interface NoOverride extends B, C {
    }

    @RedirectSet
    public interface B {
        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
        abstract class Foo_to_Bar_redirects {
            @MethodRedirect(@MethodSig("foo()V"))
            public native void bar();
        }
    }

    @RedirectSet
    public interface C {
        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(FooBaz.class))
        abstract class Foo_to_FooBaz_redirects {
            @MethodRedirect(@MethodSig("foo()V"))
            public native void foobaz();
        }
    }
}
