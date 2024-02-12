package io.github.notstirred.dasm.test.tests.t3;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;

@Dasm(T3Dasm.T3Set.class)
public class T3Dasm {
    @TransformFromMethod(value = @MethodSig("method1(Ljava/lang/String;)V"))
    native String method2(String param);

    @RedirectSet
    public interface T3Set {
        @RedirectContainer(owner = @Ref(String.class))
        abstract class A {
            @MethodRedirect(@MethodSig(ret = @Ref(int.class), name = "hashCode", args = { }))
            public native int length();
        }
    }
}
