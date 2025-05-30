package io.github.notstirred.dasm.test.tests.integration.inter_constructor_to_factory;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import java.io.File;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Constructor to factory redirects with an additional different type redirect
 */
@Dasm(TestConstructorToFactory.Set.class)
public class TestConstructorToFactory extends BaseMethodTest {
    public TestConstructorToFactory() {
        super(single(ConstructorToFactoryInput.class, ConstructorToFactoryOutput.class, TestConstructorToFactory.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()Ljava/lang/Object;"))
    public native String method1out(String param);

    @TransformFromMethod(value = @MethodSig("method2()V"), useRedirectSets = InnerConstructorSet.class)
    public native void method2out1();

    @TransformFromMethod(value = @MethodSig("method2()V"), useRedirectSets = OuterConstructorSet.class)
    public native void method2out2();

    @TransformFromMethod(value = @MethodSig("method3()V"), useRedirectSets = A.class)
    public native void method3out();

    @RedirectSet
    public interface A {
    }

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }

        @InterOwnerContainer(from = @Ref(Object.class), to = @Ref(TestConstructorToFactory.class))
        abstract class B {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {}))
            static native String createString();
        }
    }

    @RedirectSet
    public interface InnerConstructorSet {
        @InterOwnerContainer(from = @Ref(File.class), to = @Ref(TestConstructorToFactory.class))
        abstract class B {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = @Ref(String.class)))
            static native File fromString(String s);
        }
    }

    @RedirectSet
    public interface OuterConstructorSet {
        @InterOwnerContainer(from = @Ref(File.class), to = @Ref(TestConstructorToFactory.class))
        abstract class B {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {@Ref(File.class), @Ref(String.class)}))
            static native File fromParentWithChild(File parent, String s);
        }
    }

    public static String createString() {
        return "";
    }

    public static File fromString(String s) {
        return new File(s);
    }

    public static File fromParentWithChild(File parent, String s) {
        return new File(parent, s);
    }
}
