package io.github.notstirred.dasm.test.targets.inherited_transforms;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.test.targets.functional_interface.IBar;
import io.github.notstirred.dasm.test.tests.unit.inherited_transforms.TestUnitInheritedTransformsAddToSets;

public class Bar implements IBar {
    public static Bar instance;

    public int barField;
    public int barField2;
    @AddFieldToSets(containers = TestUnitInheritedTransformsAddToSets.A.Foo_to_Bar_redirects.class, owner = @Ref(Foo.class), field = @FieldSig(type = @Ref(int.class), name = "fooField3"))
    public int barField3;

    public void bar() {
    }

    public static Bar create() {
        return new Bar();
    }

    public void setBarField2(int barField2) {
    }

    public int getBarField2() {
        return barField2;
    }

    @Override
    public void bar(Bar bar) {
    }

    @AddMethodToSets(containers = TestUnitInheritedTransformsAddToSets.A.Foo_to_Bar_redirects.class, owner = @Ref(Foo.class), method = @MethodSig(ret = @Ref(void.class), name = "fooRedirected", args = {}))
    public void barRedirected() {
    }
}
