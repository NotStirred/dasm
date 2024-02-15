package io.github.notstirred.dasm.test.tests.t6;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;

import java.io.File;

@Dasm(T6Dasm.T6Set.class)
public class T6Dasm {
    @TransformFromMethod(value = @MethodSig("method1()Ljava/lang/Object;"))
    public native String method1out(String param);

    @TransformFromMethod(value = @MethodSig("method2()V"), useRedirectSets = InnerConstructorSet.class)
    public native void method2out1();

    @TransformFromMethod(value = @MethodSig("method2()V"), useRedirectSets = OuterConstructorSet.class)
    public native void method2out2();

    @TransformFromMethod(value = @MethodSig("method3()V"), useRedirectSets = A.class)
    public native void method3out();

    @RedirectSet
    public interface A { }

    @RedirectSet
    public interface T6Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A { }

        @RedirectContainer(owner = @Ref(Object.class), newOwner = @Ref(T6Dasm.class))
        abstract class B {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { }))
            public native String createString();
        }
    }

    @RedirectSet
    public interface InnerConstructorSet {
        @RedirectContainer(owner = @Ref(File.class), newOwner = @Ref(T6Dasm.class))
        abstract class B {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = @Ref(String.class)))
            public native File fromString(String s);
        }
    }

    @RedirectSet
    public interface OuterConstructorSet {
        @RedirectContainer(owner = @Ref(File.class), newOwner = @Ref(T6Dasm.class))
        abstract class B {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = { @Ref(File.class), @Ref(String.class) }))
            public native File fromParentWithChild(File parent, String s);
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
