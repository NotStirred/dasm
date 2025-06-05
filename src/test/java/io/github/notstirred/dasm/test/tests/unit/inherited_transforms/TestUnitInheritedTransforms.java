package io.github.notstirred.dasm.test.tests.unit.inherited_transforms;

import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.test.TestHarness;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.BarBaz;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.targets.inherited_transforms.FooBaz;
import io.github.notstirred.dasm.transformer.data.TransformRedirects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.objectweb.asm.Type;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Dasm(TestUnitInheritedTransforms.A.class)
public class TestUnitInheritedTransforms {
    @Test
    public void testInheritedTransformsArePresent() throws DasmException {
        var redirects = TestHarness.getRedirectsFor(TestUnitInheritedTransforms.class);
        TransformRedirects transformRedirects = new TransformRedirects(redirects.get().stream().findFirst().get().redirectSets(), MappingsProvider.IDENTITY);
        Collection<MethodRedirectImpl> methodRedirects = transformRedirects.methodRedirects().values();
        assertEquals(2, methodRedirects.size(), "Missing inherited method redirects");
        assertTrue(methodRedirects.stream().anyMatch(redirect ->
                redirect.srcMethod().owner().equals(Type.getType(Foo.class))
                        && redirect.srcMethod().mappingsOwner().equals(Type.getType(Foo.class))
                        && redirect.dstOwner().equals(Type.getType(Bar.class))
                        && !redirect.isStatic()
                        && !redirect.isDstOwnerInterface()));
        assertTrue(methodRedirects.stream().anyMatch(redirect ->
                redirect.srcMethod().owner().equals(Type.getType(FooBaz.class))
                        && redirect.srcMethod().mappingsOwner().equals(Type.getType(FooBaz.class))
                        && redirect.dstOwner().equals(Type.getType(BarBaz.class))
                        && !redirect.isStatic()
                        && !redirect.isDstOwnerInterface()));

        Collection<FieldRedirectImpl> fieldRedirects = transformRedirects.fieldRedirects().values();
        assertEquals(2, fieldRedirects.size(), "Missing inherited field redirects");
        assertTrue(fieldRedirects.stream().anyMatch(redirect ->
                redirect.srcField().owner().equals(Type.getType(Foo.class))
                        && redirect.srcField().mappingsOwner().equals(Type.getType(Foo.class))
                        && redirect.dstOwner().equals(Type.getType(Bar.class))));
        assertTrue(fieldRedirects.stream().anyMatch(redirect ->
                redirect.srcField().owner().equals(Type.getType(FooBaz.class))
                        && redirect.srcField().mappingsOwner().equals(Type.getType(FooBaz.class))
                        && redirect.dstOwner().equals(Type.getType(BarBaz.class))));
    }

    @TransformFromMethod(@MethodSig("dummy()V"))
    private native void dummy();

    @RedirectSet
    interface A {
        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
        abstract class Foo_to_Bar_redirects {
            @MethodRedirect(@MethodSig("foo()V"))
            abstract void bar();

            @FieldRedirect(@FieldSig(type = @Ref(int.class), name = "a"))
            public int b;
        }

        @TypeRedirect(from = @Ref(FooBaz.class), to = @Ref(BarBaz.class))
        abstract class FooBaz_to_BarBaz_redirects extends Foo_to_Bar_redirects {
        }
    }
}
