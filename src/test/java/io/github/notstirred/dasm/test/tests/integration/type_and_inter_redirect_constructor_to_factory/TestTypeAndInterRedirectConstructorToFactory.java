package io.github.notstirred.dasm.test.tests.integration.type_and_inter_redirect_constructor_to_factory;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.BFactory;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * This tests the case where there is both a {@link TypeRedirect} and an {@link InterOwnerContainer} on the same source type.
 * <p>
 * The {@link InterOwnerContainer} contains a {@link ConstructorToFactoryRedirect} that changes the owner to a <b><u>different
 * type</u></b> to the type redirect {@link TypeRedirect#to() to} type.
 */
@Dasm(value = TestTypeAndInterRedirectConstructorToFactory.Set.class, target = @Ref(TypeAndInterRedirectConstructorToFactoryInput.class))
public class TestTypeAndInterRedirectConstructorToFactory extends BaseMethodTest {
    public TestTypeAndInterRedirectConstructorToFactory() {
        super(single(TypeAndInterRedirectConstructorToFactoryInput.class, TypeAndInterRedirectConstructorToFactoryOutput.class, TestTypeAndInterRedirectConstructorToFactory.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()Lio/github/notstirred/dasm/test/targets/A;"))
    public native String method1out(String param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(A.class), to = @Ref(B.class))
        abstract class A_to_B_redirects {
            @MethodRedirect(@MethodSig("doAThings()V"))
            native void doBThings();
        }

        @InterOwnerContainer(from = @Ref(A.class), to = @Ref(BFactory.class))
        abstract class A_to_BFactory_redirects {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {}))
            public static native B createB();
        }
    }
}
