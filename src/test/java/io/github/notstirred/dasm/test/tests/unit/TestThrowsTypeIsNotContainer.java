package io.github.notstirred.dasm.test.tests.unit;

import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.test.TestHarness;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Dasm({})
public class TestThrowsTypeIsNotContainer {
    @Test
    public void testInheritedTransformsArePresent() {
        var redirects = TestHarness.getRedirectsFor(TestThrowsTypeIsNotContainer.class);
        assertEquals(Optional.empty(), redirects.first());
        assertEquals(AnnotationParser.TypeIsNotAContainer.class, redirects.second().get(0).sourceClass);
    }

    @AddMethodToSets(containers = B.class, method = "foo()V")
    private void bar() {
    }

    abstract class B {
    }
}
