package io.github.notstirred.dasm.test.tests.add_parameter_multiple_static;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.AddUnusedParam;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

@Dasm(TestAddParameterMultipleStatic.Set.class)
public class TestAddParameterMultipleStatic extends BaseMethodTest {
    public TestAddParameterMultipleStatic() {
        super(single(AddParameterMultipleStaticInput.class, AddParameterMultipleStaticOutput.class, TestAddParameterMultipleStatic.class));
    }

    @TransformFromMethod(value = @MethodSig("method1static(Ljava.lang.Object;)[Ljava.lang.Object;"))
    public native static float[] method1out1static(@AddUnusedParam float[][] c, float a, @AddUnusedParam float[][] b);

    @TransformFromMethod(value = @MethodSig("method1static(Ljava.lang.Object;)[Ljava.lang.Object;"))
    public native static float[] method1out2static(float a, @AddUnusedParam float[][] b, @AddUnusedParam int c, @AddUnusedParam int d);

    @TransformFromMethod(value = @MethodSig("method2static(Ljava.lang.String;Ljava.lang.String;Ljava.lang.String;)[Ljava.lang.String;"))
    public native static String[] method2out1static(@AddUnusedParam float a, @AddUnusedParam float b, String c, @AddUnusedParam int d, @AddUnusedParam double e, String f, @AddUnusedParam long g, String h, @AddUnusedParam boolean i);

    @TransformFromMethod(value = @MethodSig("method3static(JJ)[Ljava.lang.String;"))
    public native static void method3out1static(@AddUnusedParam long c, @AddUnusedParam int d, long a, @AddUnusedParam double e, long b, @AddUnusedParam int f, @AddUnusedParam long g);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
