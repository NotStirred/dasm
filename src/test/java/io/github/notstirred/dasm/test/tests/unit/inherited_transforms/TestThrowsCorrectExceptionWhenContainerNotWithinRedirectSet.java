package io.github.notstirred.dasm.test.tests.unit.inherited_transforms;

import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.notify.Notification;
import io.github.notstirred.dasm.test.TestHarness;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Dasm(TestThrowsCorrectExceptionWhenContainerNotWithinRedirectSet.A.class)
public class TestThrowsCorrectExceptionWhenContainerNotWithinRedirectSet {
    @Test
    public void testInheritedTransformsArePresent() {
        var redirects = TestHarness.getRedirectsFor(TestThrowsCorrectExceptionWhenContainerNotWithinRedirectSet.class);
        assertEquals(Optional.empty(), redirects.first());
        Notification notification = redirects.second().get(0);
        assertEquals(AnnotationParser.ContainerNotWithinRedirectSet.class, notification.sourceClass);
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
