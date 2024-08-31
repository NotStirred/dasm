package io.github.notstirred.dasm.test.tests.inter_onearg_constructor_to_factory;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.NewSoup;
import io.github.notstirred.dasm.test.targets.Soup;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * Constructor to factory redirects with an additional different type redirect
 */
@Dasm(TestInterOneArgConstructorToFactory.Set.class)
public class TestInterOneArgConstructorToFactory extends BaseMethodTest {
    public TestInterOneArgConstructorToFactory() {
        super(single(InterOneArgConstructorToFactoryInput.class, InterOneArgConstructorToFactoryOutput.class, TestInterOneArgConstructorToFactory.class));
    }

    @TransformFromMethod(value = @MethodSig("method1()Lio/github/notstirred/dasm/test/targets/Soup;"))
    public native String method1out(String param);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Soup.class), to = @Ref(NewSoup.class))
        abstract class Soup_redirects {
            @ConstructorToFactoryRedirect(@ConstructorMethodSig(args = {@Ref(int.class)}))
            static native NewSoup create(int foo);
        }
    }
}
