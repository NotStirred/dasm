package io.github.notstirred.dasm.test.tests.integration.set_inheritance_wholeclass;

import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;

/**
 * Subtest of {@link TestSetInheritanceWholeClass1}
 */
@TransformFromClass(value = @Ref(SetInheritanceWholeClassInput.class), sets = SetInheritanceWholeClassDerivedSetB.class)
public class TestSetInheritanceWholeClass2 { }
