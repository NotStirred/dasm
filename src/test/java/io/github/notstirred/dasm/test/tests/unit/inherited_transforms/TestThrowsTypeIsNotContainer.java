package io.github.notstirred.dasm.test.tests.unit.inherited_transforms;

import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.test.TestHarness;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Dasm({})
public class TestThrowsTypeIsNotContainer {
    @Test
    public void testInheritedTransformsArePresent() {
        DasmException e = assertThrows(DasmException.class, () -> TestHarness.getRedirectsFor(TestThrowsTypeIsNotContainer.class));
        assertEquals(AnnotationParser.TypeIsNotAContainer.class, e.getSuppressed()[0].getClass());
    }

    @AddMethodToSets(containers = B.class, method = @MethodSig(ret = @Ref(void.class), name = "foo", args = {}))
    private void bar() {
    }

    abstract class B {
    }
}
