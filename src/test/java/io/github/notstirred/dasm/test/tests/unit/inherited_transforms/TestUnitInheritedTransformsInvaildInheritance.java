package io.github.notstirred.dasm.test.tests.unit.inherited_transforms;

import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.test.TestHarness;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.BarBaz;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.targets.inherited_transforms.FooBaz;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Dasm(TestUnitInheritedTransformsInvaildInheritance.B.class)
public class TestUnitInheritedTransformsInvaildInheritance {
    @Test
    public void testInheritedTransformsArePresent() {
        var exception = assertThrowsExactly(DasmException.class, () -> TestHarness.getRedirectsFor(TestUnitInheritedTransformsInvaildInheritance.class));
        assertEquals(RedirectSetImpl.SuperTypeInInvalidRedirectSet.class, exception.getSuppressed()[0].getClass());
    }

    @TransformFromMethod(@MethodSig("dummy()V"))
    private native void dummy();

    @RedirectSet
    interface A {
        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
        abstract class Foo_to_Bar_redirects {
        }
    }

    @RedirectSet
    interface B {
        @TypeRedirect(from = @Ref(FooBaz.class), to = @Ref(BarBaz.class))
        abstract class FooBaz_to_BarBaz_redirects extends A.Foo_to_Bar_redirects {
        }
    }
}
