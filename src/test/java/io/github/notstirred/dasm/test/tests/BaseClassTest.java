package io.github.notstirred.dasm.test.tests;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static io.github.notstirred.dasm.test.TestHarness.verifyClassTransformValid;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseClassTest {
    private final List<TestData> data;

    public BaseClassTest(List<TestData> data) {
        this.data = data;
    }

    private List<TestData> data() {
        return this.data;
    }

    @MethodSource("data")
    @ParameterizedTest
    protected void test(TestData datum) {
        verifyClassTransformValid(datum.input(), datum.output(), datum.additionalDasm());
    }
}
