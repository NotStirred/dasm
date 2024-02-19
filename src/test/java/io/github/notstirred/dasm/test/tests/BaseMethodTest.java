package io.github.notstirred.dasm.test.tests;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static io.github.notstirred.dasm.test.TestHarness.verifyMethodTransformsValid;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseMethodTest {
    private final List<TestData> data;

    public BaseMethodTest(List<TestData> data) {
        this.data = data;
    }

    private List<TestData> data() {
        return this.data;
    }

    @MethodSource("data")
    @ParameterizedTest
    protected void test(TestData datum) {
        verifyMethodTransformsValid(datum.input(), datum.output(), datum.additionalDasm());
    }
}
