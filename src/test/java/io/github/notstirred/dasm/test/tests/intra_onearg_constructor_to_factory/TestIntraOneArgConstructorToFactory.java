package io.github.notstirred.dasm.test.tests.intra_onearg_constructor_to_factory;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.Soup;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * Constructor to factory redirects with an additional different type redirect
 */
@Dasm(TestIntraOneArgConstructorToFactory.Set.class)
public class TestIntraOneArgConstructorToFactory extends BaseMethodTest {
    public TestIntraOneArgConstructorToFactory() {
        super(single(IntraOneArgConstructorToFactoryInput.class, IntraOneArgConstructorToFactoryOutput.class, TestIntraOneArgConstructorToFactory.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()Lio/github/notstirred/dasm/test/targets/Soup;"))
    public native String method1out(String param);

    @RedirectSet
    public interface Set {
        @IntraOwnerContainer(owner = @Ref(Soup.class))
        abstract class Soup_redirects {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {@Ref(int.class)}))
            static native Soup create(int foo);
        }
    }
}