package io.github.notstirred.dasm.test.tests.integration.implicit_lambda_functional_interface_method_name_redirect;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformMethod;
import io.github.notstirred.dasm.test.targets.functional_interface.IBar;
import io.github.notstirred.dasm.test.targets.functional_interface.IFoo;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * TODO
 */
@Dasm(value = TestImplicitLambdaFunctionalInterfaceMethodNameRedirect.Set.class, target = @Ref(ImplicitLambdaFunctionalInterfaceMethodRedirectNameInput.class))
public class TestImplicitLambdaFunctionalInterfaceMethodNameRedirect extends BaseMethodTest {
    public TestImplicitLambdaFunctionalInterfaceMethodNameRedirect() {
        super(single(ImplicitLambdaFunctionalInterfaceMethodRedirectNameInput.class, ImplicitLambdaFunctionalInterfaceMethodRedirectNameOutput.class, TestImplicitLambdaFunctionalInterfaceMethodNameRedirect.class));
    }

    @TransformMethod("method1()V")
    public native void method1();

    @RedirectSet
    interface Set {
        @TypeRedirect(from = @Ref(IFoo.class), to = @Ref(IBar.class))
        interface IFoo_to_IBar_redirects {
            @MethodRedirect("foo(Lio/github/notstirred/dasm/test/targets/inherited_transforms/Foo;)V")
            void bar(Bar bar);
        }

        @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
        class Foo_to_Bar_redirects {
        }
    }
}
