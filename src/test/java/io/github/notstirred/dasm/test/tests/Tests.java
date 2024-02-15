package io.github.notstirred.dasm.test.tests;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.test.tests.addtosets.TestAddToSetsDasm;
import io.github.notstirred.dasm.test.tests.addtosets.TestAddToSetsInput;
import io.github.notstirred.dasm.test.tests.addtosets.TestAddToSetsOutput;
import io.github.notstirred.dasm.test.tests.setinheritance.TestSetInheritanceDasm;
import io.github.notstirred.dasm.test.tests.setinheritance.TestSetInheritanceInput;
import io.github.notstirred.dasm.test.tests.setinheritance.TestSetInheritanceOutput;
import io.github.notstirred.dasm.test.tests.setinheritance_wholeclass.*;
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
import io.github.notstirred.dasm.test.tests.t5.T5Dasm;
import io.github.notstirred.dasm.test.tests.t5.T5Input;
import io.github.notstirred.dasm.test.tests.t5.T5Output;
import io.github.notstirred.dasm.test.tests.t6.T6Dasm;
import io.github.notstirred.dasm.test.tests.t6.T6Input;
import io.github.notstirred.dasm.test.tests.t6.T6Output;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static io.github.notstirred.dasm.test.TestHarness.verifyClassTransformValid;
import static io.github.notstirred.dasm.test.TestHarness.verifyMethodTransformsValid;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class Tests {
    /**
     * Verify that constructors are properly redirected.
     * new Object() -> new String()
     */
    @Test public void testTrivialConstructorRedirects() {
        verifyMethodTransformsValid(T1Input.class, T1Output.class, T1Dasm.class);
    }

    /**
     * Verify that type redirects implicitly do self method redirects.
     * Object#hashCode() -> String#hashCode()
     */
    @Test public void testTrivialImplicitMethodRedirectsDueToTypeRedirects() {
        verifyMethodTransformsValid(T2Input.class, T2Output.class, T2Dasm.class);
    }

    /**
     * Verify that method redirects work
     * String#hashCode() -> String#length()
     */
    @Test public void testTrivialMethodRedirect() {
        verifyMethodTransformsValid(T3Input.class, T3Output.class, T3Dasm.class);
    }

    /**
     * Verify that type redirects work for method parameters and return types.
     * Object -> String
     */
    @Test public void testTrivialTypeRedirect() {
        verifyMethodTransformsValid(T4Input.class, T4Output.class, T4Dasm.class);
    }

    /**
     * Type/field/method redirects in a copyFrom method transform.
     * Object -> String
     */
    @Test public void testCopyFromTransform() {
        verifyMethodTransformsValid(T5Input.class, T5Output.class, T5Dasm.class);
    }

    /**
     * Constructor to factory redirects with an additional different type redirect
     */
    @Test public void testConstructorToFactoryRedirects() {
        verifyMethodTransformsValid(T6Input.class, T6Output.class, T6Dasm.class);
    }

    /**
     * A trivial test for a static {@link AddMethodToSets}
     */
    @Test public void testAddMethodToSets() {
        verifyMethodTransformsValid(TestAddToSetsInput.class, TestAddToSetsOutput.class, TestAddToSetsDasm.class);
    }

    /**
     * A trivial test for set inheritance
     */
    @Test public void testSetInheritance() {
        verifyMethodTransformsValid(TestSetInheritanceInput.class, TestSetInheritanceOutput.class, TestSetInheritanceDasm.class);
    }

    /**
     * A trivial test for set inheritance
     */
    @Test public void testSetInheritanceWholeClass() {
        verifyClassTransformValid(
                TestSetInheritanceWholeClassInput.class, TestSetInheritanceWholeClassOutput1.class, TestSetInheritanceWholeClassDasm1.class);
        verifyClassTransformValid(
                TestSetInheritanceWholeClassInput.class, TestSetInheritanceWholeClassOutput2.class, TestSetInheritanceWholeClassDasm2.class);
    }
}
