package io.github.notstirred.dasm.test.tests.visibility_changes;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.api.annotations.transform.Visibility;
import io.github.notstirred.dasm.test.targets.CubePos;
import io.github.notstirred.dasm.test.tests.BaseMethodTest;

import static io.github.notstirred.dasm.test.tests.TestData.single;

/**
 * A trivial test for changing method visibility
 */
@Dasm(TestVisibilityChanges.Set.class)
public class TestVisibilityChanges extends BaseMethodTest {
    public TestVisibilityChanges() {
        super(single(VisibilityChangesInput.class, VisibilityChangesOutput.class, TestVisibilityChanges.class));
    }

    @TransformFromMethod(value = @MethodSig("pri()V"))
    private void pri2() {
    }

    @TransformFromMethod(value = @MethodSig("pri()V"), visibility = Visibility.PUBLIC)
    public void pri_pub() {
    }

    @TransformFromMethod(value = @MethodSig("pri()V"), visibility = Visibility.PROTECTED)
    protected void pri_pro() {
    }

    @TransformFromMethod(value = @MethodSig("pri()V"), visibility = Visibility.PACKAGE_PROTECTED)
    void pri_pp() {
    }

    @TransformFromMethod(value = @MethodSig("pro()V"))
    protected void pro2() {
    }

    @TransformFromMethod(value = @MethodSig("pro()V"), visibility = Visibility.PRIVATE)
    private void pro_pri() {
    }

    @TransformFromMethod(value = @MethodSig("pro()V"), visibility = Visibility.PUBLIC)
    public void pro_pub() {
    }

    @TransformFromMethod(value = @MethodSig("pro()V"), visibility = Visibility.PACKAGE_PROTECTED)
    void pro_pp() {
    }

    @TransformFromMethod(value = @MethodSig("pub()V"))
    public void pub2() {
    }

    @TransformFromMethod(value = @MethodSig("pub()V"), visibility = Visibility.PRIVATE)
    private void pub_pri() {
    }

    @TransformFromMethod(value = @MethodSig("pub()V"), visibility = Visibility.PROTECTED)
    protected void pub_pro() {
    }

    @TransformFromMethod(value = @MethodSig("pub()V"), visibility = Visibility.PACKAGE_PROTECTED)
    void pub_pp() {
    }

    @TransformFromMethod(value = @MethodSig("package_pro()V"))
    void package_pro2() {
    }

    @TransformFromMethod(value = @MethodSig("package_pro()V"), visibility = Visibility.PRIVATE)
    private void package_pro_pri() {
    }

    @TransformFromMethod(value = @MethodSig("package_pro()V"), visibility = Visibility.PUBLIC)
    public void package_pro_pub() {
    }

    @TransformFromMethod(value = @MethodSig("package_pro()V"), visibility = Visibility.PROTECTED)
    protected void package_pro_pro() {
    }

    native String method1out();

    @RedirectSet
    public interface Set {
        @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
        abstract class A {
        }
    }

    @AddMethodToSets(owner = @Ref(CubePos.class), method = @MethodSig(name = "from", ret = @Ref(CubePos.class), args = {@Ref(long.class)}), sets = Set.class)
    public static CubePos testFoo(long l) {
        return null;
    }
}