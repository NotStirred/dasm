package io.github.notstirred.dasm.test.tests.add_parameter_multiple;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.AddUnusedParam;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

@Dasm(TestAddParameterMultiple.Set.class)
public class TestAddParameterMultiple extends BaseMethodTest {
    public TestAddParameterMultiple() {
        super(single(AddParameterMultipleInput.class, AddParameterMultipleOutput.class, TestAddParameterMultiple.class));
    }

    @TransformFromMethod(value = @MethodSig("method1(Ljava.lang.Object;)[Ljava.lang.Object;"))
    public native float[] method1out1(@AddUnusedParam float[][] c, float a, @AddUnusedParam float[][] b);

    @TransformFromMethod(value = @MethodSig("method1(Ljava.lang.Object;)[Ljava.lang.Object;"))
    public native float[] method1out2(float a, @AddUnusedParam float[][] b, @AddUnusedParam int c, @AddUnusedParam int d);

    @TransformFromMethod(value = @MethodSig("method2(Ljava.lang.String;Ljava.lang.String;Ljava.lang.String;)[Ljava.lang.String;"))
    public native String[] method2out1(@AddUnusedParam float a, @AddUnusedParam float b, String c, @AddUnusedParam int d, @AddUnusedParam double e, String f, @AddUnusedParam long g, String h, @AddUnusedParam boolean i);

    @TransformFromMethod(value = @MethodSig("method3(JJ)[Ljava.lang.String;"))
    public native void method3out1(@AddUnusedParam long c, @AddUnusedParam int d, long a, @AddUnusedParam double e, long b, @AddUnusedParam int f, @AddUnusedParam long g);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
