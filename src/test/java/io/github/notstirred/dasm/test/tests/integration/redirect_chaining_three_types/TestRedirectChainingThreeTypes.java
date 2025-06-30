package io.github.notstirred.dasm.test.tests.integration.redirect_chaining_three_types;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.test.targets.A;
import io.github.notstirred.dasm.test.targets.B;
import io.github.notstirred.dasm.test.targets.C;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

@Dasm(value = TestRedirectChainingThreeTypes.Set.class, target = @Ref(RedirectChainingThreeTypesInput.class))
public class TestRedirectChainingThreeTypes extends BaseMethodTest {
    public TestRedirectChainingThreeTypes() {
        super(single(RedirectChainingThreeTypesInput.class, RedirectChainingThreeTypesOutput.class, TestRedirectChainingThreeTypes.class));
    }

    @TransformFromMethod("method1(Lio/github/notstirred/dasm/test/targets/A;)V")
    public native void method1out(A a);

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(A.class), to = @Ref(B.class))
        abstract class A_to_B {
        }

        @TypeRedirect(from = @Ref(B.class), to = @Ref(C.class))
        abstract class B_to_C {
        }
    }
}
