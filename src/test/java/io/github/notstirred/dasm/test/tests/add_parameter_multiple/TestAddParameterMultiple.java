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

    @AddUnusedParam(type = @Ref(float[][].class), index = 1)
    @AddUnusedParam(type = @Ref(float[][].class), index = 0)
    @TransformFromMethod(value = @MethodSig("method1(Ljava.lang.Object;)[Ljava.lang.Object;"))
    public native float[] method1out1(float[] c, float a, float[][] b);

    @AddUnusedParam(type = @Ref(float[][].class), index = 1)
    @AddUnusedParam(type = @Ref(int.class), index = 1)
    @AddUnusedParam(type = @Ref(int.class), index = 1)
    @TransformFromMethod(value = @MethodSig("method1(Ljava.lang.Object;)[Ljava.lang.Object;"))
    public native float[] method1out2(float a, float[][] b, int c, int d);


    @AddUnusedParam(type = @Ref(float.class), index = 0)
    @AddUnusedParam(type = @Ref(float.class), index = 0)
    @AddUnusedParam(type = @Ref(int.class), index = 1)
    @AddUnusedParam(type = @Ref(double.class), index = 1)
    @AddUnusedParam(type = @Ref(long.class), index = 2)
    @AddUnusedParam(type = @Ref(boolean.class), index = 3)
    @TransformFromMethod(value = @MethodSig("method2(Ljava.lang.String;Ljava.lang.String;Ljava.lang.String;)[Ljava.lang.String;"))
    public native String[] method2out1(float a, float b, String c, int d, double e, String f, long g, String h, boolean i);

    @AddUnusedParam(type = @Ref(long.class), index = 0)
    @AddUnusedParam(type = @Ref(int.class), index = 0)
    @AddUnusedParam(type = @Ref(double.class), index = 1)
    @AddUnusedParam(type = @Ref(int.class), index = 2)
    @AddUnusedParam(type = @Ref(long.class), index = 2)
    @TransformFromMethod(value = @MethodSig("method3(JJ)[Ljava.lang.String;"))
    public native void method3out1(long c, int d, long a, double e, long b, int f, long g);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
