package io.github.notstirred.dasm.test.tests.inplace_method_transform;
            
public class InplaceMethodTransformTestInput {
    public void method1(Float a) {
        System.out.printf("%s", a);
    }

    // different method with dst signature to be overwritten.
    public void method1(String a) { }
}
