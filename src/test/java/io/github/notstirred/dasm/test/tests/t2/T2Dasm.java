package io.github.notstirred.dasm.test.tests.t2;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;


@Dasm(T2Dasm.T2Set.class)
public class T2Dasm {
    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/Float;)V"))
    native String method1(String param);

    @RedirectSet
    public interface T2Set {
        @TypeRedirect(from = @Ref(Float.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}
