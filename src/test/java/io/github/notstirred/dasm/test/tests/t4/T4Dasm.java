package io.github.notstirred.dasm.test.tests.t4;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;

@Dasm(T4Dasm.T4Set.class)
public class T4Dasm {
    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/Object;)Ljava/lang/Object;"))
    public native String method2(String param);

    @RedirectSet
    public interface T4Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
