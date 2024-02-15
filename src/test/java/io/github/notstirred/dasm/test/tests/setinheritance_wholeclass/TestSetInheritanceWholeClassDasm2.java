package io.github.notstirred.dasm.test.tests.setinheritance_wholeclass;

import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;

@TransformFromClass(value = @Ref(TestSetInheritanceWholeClassInput.class), sets = TestSetInheritanceWholeClassDerivedSetB.class)
public class TestSetInheritanceWholeClassDasm2 {
}
