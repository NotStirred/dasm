package io.github.notstirred.dasm.test.tests.integration.inter_constructor_to_factory;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import java.io.File;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Constructor to factory redirects with an additional different type redirect
 */
@Dasm(value = TestConstructorToFactory.Set.class, target = @Ref(ConstructorToFactoryInput.class))
public class TestConstructorToFactory extends BaseMethodTest {
    public TestConstructorToFactory() {
        super(single(ConstructorToFactoryInput.class, ConstructorToFactoryOutput.class, TestConstructorToFactory.class));
    }

    @TransformFromMethod("method1()Ljava/lang/Object;")
    public native String method1out(String param);

    @TransformFromMethod(value = "method2()V", useRedirectSets = InnerConstructorSet.class)
    public native void method2out1();

    @TransformFromMethod(value = "method2()V", useRedirectSets = OuterConstructorSet.class)
    public native void method2out2();

    @TransformFromMethod(value = "method3()V", useRedirectSets = A.class)
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
            @ConstructorToFactoryRedirect("<init>()V")
            static native String createString();
        }
    }

    @RedirectSet
    public interface InnerConstructorSet {
        @InterOwnerContainer(from = @Ref(File.class), to = @Ref(TestConstructorToFactory.class))
        abstract class B {
            @ConstructorToFactoryRedirect("<init>(Ljava/lang/String;)V")
            static native File fromString(String s);
        }
    }

    @RedirectSet
    public interface OuterConstructorSet {
        @InterOwnerContainer(from = @Ref(File.class), to = @Ref(TestConstructorToFactory.class))
        abstract class B {
            @ConstructorToFactoryRedirect("<init>(Ljava/io/File;Ljava/lang/String;)V")
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
