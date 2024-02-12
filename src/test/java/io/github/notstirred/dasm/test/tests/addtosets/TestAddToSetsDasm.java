package io.github.notstirred.dasm.test.tests.addtosets;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.CubePos;

@Dasm(TestAddToSetsDasm.Set.class)
public class TestAddToSetsDasm {
    @TransformFromMethod(value = @MethodSig("method1()V"))
    native String method2();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A { }
    }

    @AddMethodToSets(owner = @Ref(CubePos.class), ownerIsInterface = false, method = @MethodSig(name = "fromLong", ret = @Ref(CubePos.class), args = { }), sets = Set.class)
    public static CubePos testFoo(long l) {
        return null;
    }
}