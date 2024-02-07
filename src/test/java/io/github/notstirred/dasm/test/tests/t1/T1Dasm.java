package io.github.notstirred.dasm.test.tests.t1;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;

@Dasm(T1Dasm.T1Set.class)
public class T1Dasm {
    @TransformFromMethod(value = @MethodSig("method1()Ljava/lang/Object;"))
    native String method2();

    @RedirectSet
    public interface T1Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A { }
    }
}