package io.github.notstirred.dasm.test.tests.integration.container_inheritance.interface_to_class;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.*;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * Test that when interfacicity of an inherited redirect changes everything works properly.
 */
@Dasm(value = TestInterfaceToClass.Set.class, target = @Ref(InterfaceToClassInput.class))
public class TestInterfaceToClass extends BaseMethodTest {
    public TestInterfaceToClass() {
        super(single(InterfaceToClassInput.class, InterfaceToClassOutput.class, TestInterfaceToClass.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()V"))
    native void method1out();

    @TransformFromMethod(value = @MethodSig("method2()V"))
    native void method2out();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(BFactory.class), to = @Ref(AFactory.class))
        interface BFactory_to_AFactory_redirects {
            @MethodRedirect(@MethodSig(ret = @Ref(B.class), name = "createB", args = {}))
            static A createA() {
                throw new IllegalStateException("DASM FAILED TO APPLY");
            }
        }

        @TypeRedirect(from = @Ref(SubBFactory.class), to = @Ref(SubAFactory.class))
        abstract class SubBFactory_to_SubAFactory_redirects implements BFactory_to_AFactory_redirects {
        }

        @TypeRedirect(from = @Ref(B.class), to = @Ref(A.class))
        abstract class B_to_A_redirects {
        }
    }
}
