package io.github.notstirred.dasm.test.tests;

import java.util.Collections;
import java.util.List;

public record TestData(Class<?> input, Class<?> output, Class<?> additionalDasm) {
    public static List<TestData> single(Class<?> input, Class<?> output, Class<?> additionalDasm) {
        return Collections.singletonList(new TestData(input, output, additionalDasm));
    }

    @Override public String toString() {
        return "TestData [ input:" + input.getSimpleName() + ", output:" + output.getSimpleName() + ", additionalDasm:" + additionalDasm.getSimpleName() + " ]";
    }
}