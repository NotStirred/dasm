package io.github.notstirred.dasm.test.tests;

import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.test.tests.t1.T1Dasm;
import io.github.notstirred.dasm.test.tests.t1.T1Input;
import io.github.notstirred.dasm.test.tests.t1.T1Output;
import io.github.notstirred.dasm.test.tests.t2.T2Dasm;
import io.github.notstirred.dasm.test.tests.t2.T2Input;
import io.github.notstirred.dasm.test.tests.t2.T2Output;
import io.github.notstirred.dasm.test.tests.t3.T3Dasm;
import io.github.notstirred.dasm.test.tests.t3.T3Input;
import io.github.notstirred.dasm.test.tests.t3.T3Output;
import io.github.notstirred.dasm.test.tests.t4.T4Dasm;
import io.github.notstirred.dasm.test.tests.t4.T4Input;
import io.github.notstirred.dasm.test.tests.t4.T4Output;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.github.notstirred.dasm.test.TestHarness.verifyTransformValid;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {
    /**
     * Verify that constructors are properly redirected.
     * new Object() -> new String()
     */
    @Test public void testTrivialConstructorRedirects() throws DasmWrappedExceptions {
        verifyTransformValid(T1Input.class, T1Output.class, T1Dasm.class);
    }

    /**
     * Verify that type redirects implicitly do self method redirects.
     * Object#hashCode() -> String#hashCode()
     */
    @Test public void testTrivialImplicitMethodRedirectsDueToTypeRedirects() throws DasmWrappedExceptions {
        verifyTransformValid(T2Input.class, T2Output.class, T2Dasm.class);
    }

    /**
     * Verify that method redirects work
     * String#hashCode() -> String#length()
     */
    @Test public void testTrivialMethodRedirect() throws DasmWrappedExceptions {
        verifyTransformValid(T3Input.class, T3Output.class, T3Dasm.class);
    }

    /**
     * Verify that type redirects work for method parameters and return types.
     * Object -> String
     */
    @Test public void testTrivialTypeRedirect() throws DasmWrappedExceptions {
        verifyTransformValid(T4Input.class, T4Output.class, T4Dasm.class);
    }
}
