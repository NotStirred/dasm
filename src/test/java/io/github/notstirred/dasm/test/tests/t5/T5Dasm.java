package io.github.notstirred.dasm.test.tests.t5;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;

@Dasm(T5Dasm.T5Set.class)
public class T5Dasm {
    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/Object;)Ljava/lang/Object;"))
    public native String method2(String param);

    @TransformFromMethod(value = @MethodSig("methodOnAnotherClass(Ljava/lang/Object;)Ljava/lang/Object;"), copyFrom = @Ref(T5Dasm.class))
    public native String methodOnAnotherClassTransformed(String param);

    @RedirectSet
    public interface T5Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A { }

        @TypeRedirect(from = @Ref(T5Dasm.class), to = @Ref(T5Input.class))
        abstract class T5DasmToT5InputRedirect { }
    }

    public Object methodOnAnotherClass(Object o) {
        return o;
    }
}
