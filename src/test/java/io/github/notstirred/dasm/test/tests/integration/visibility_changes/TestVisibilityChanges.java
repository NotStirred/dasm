package io.github.notstirred.dasm.test.tests.integration.visibility_changes;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.api.annotations.transform.Visibility;
import io.github.notstirred.dasm.test.tests.integration.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.integration.TestData.single;

/**
 * A trivial test for changing method visibility
 */
@Dasm(value = TestVisibilityChanges.Set.class, target = @Ref(VisibilityChangesInput.class))
public class TestVisibilityChanges extends BaseMethodTest {
    public TestVisibilityChanges() {
        super(single(VisibilityChangesInput.class, VisibilityChangesOutput.class, TestVisibilityChanges.class));
    }

    @TransformFromMethod("pri()V")
    private void pri2() {
    }

    @TransformFromMethod(value = "pri()V", visibility = Visibility.PUBLIC)
    public void pri_pub() {
    }

    @TransformFromMethod(value = "pri()V", visibility = Visibility.PROTECTED)
    protected void pri_pro() {
    }

    @TransformFromMethod(value = "pri()V", visibility = Visibility.PACKAGE_PROTECTED)
    void pri_pp() {
    }

    @TransformFromMethod("pro()V")
    protected void pro2() {
    }

    @TransformFromMethod(value = "pro()V", visibility = Visibility.PRIVATE)
    private void pro_pri() {
    }

    @TransformFromMethod(value = "pro()V", visibility = Visibility.PUBLIC)
    public void pro_pub() {
    }

    @TransformFromMethod(value = "pro()V", visibility = Visibility.PACKAGE_PROTECTED)
    void pro_pp() {
    }

    @TransformFromMethod("pub()V")
    public void pub2() {
    }

    @TransformFromMethod(value = "pub()V", visibility = Visibility.PRIVATE)
    private void pub_pri() {
    }

    @TransformFromMethod(value = "pub()V", visibility = Visibility.PROTECTED)
    protected void pub_pro() {
    }

    @TransformFromMethod(value = "pub()V", visibility = Visibility.PACKAGE_PROTECTED)
    void pub_pp() {
    }

    @TransformFromMethod("package_pro()V")
    void package_pro2() {
    }

    @TransformFromMethod(value = "package_pro()V", visibility = Visibility.PRIVATE)
    private void package_pro_pri() {
    }

    @TransformFromMethod(value = "package_pro()V", visibility = Visibility.PUBLIC)
    public void package_pro_pub() {
    }

    @TransformFromMethod(value = "package_pro()V", visibility = Visibility.PROTECTED)
    protected void package_pro_pro() {
    }

    native String method1out();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }
    }
}