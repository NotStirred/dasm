package io.github.notstirred.dasm.test.tests.unit.inherited_transforms;

import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.test.TestHarness;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Dasm(TestThrowsCorrectExceptionWhenContainerNotWithinRedirectSet.A.class)
public class TestThrowsCorrectExceptionWhenContainerNotWithinRedirectSet {
    @Test
    public void testInheritedTransformsArePresent() {
        DasmException e = assertThrows(DasmException.class, () -> TestHarness.getRedirectsFor(TestThrowsCorrectExceptionWhenContainerNotWithinRedirectSet.class));
        assertEquals(AnnotationParser.ContainerNotWithinRedirectSet.class, e.getSuppressed()[0].getClass());
    }

    @AddMethodToSets(containers = Foo_to_Bar_redirects.class, method = @MethodSig(ret = @Ref(void.class), name = "foo", args = {}))
    private void bar() {
    }

    @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
    abstract class Foo_to_Bar_redirects {
    }

    @RedirectSet
    interface A {
    }
}
