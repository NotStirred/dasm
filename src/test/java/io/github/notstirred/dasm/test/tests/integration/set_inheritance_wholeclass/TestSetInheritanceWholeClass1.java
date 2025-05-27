package io.github.notstirred.dasm.test.tests.integration.set_inheritance_wholeclass;

import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.test.tests.integration.BaseClassTest;
import io.github.notstirred.dasm.test.tests.integration.TestData;

import java.util.List;

/**
 * A trivial test for set inheritance
 */
@TransformFromClass(value = @Ref(SetInheritanceWholeClassInput.class), sets = SetInheritanceWholeClassDerivedSetA.class)
public class TestSetInheritanceWholeClass1 extends BaseClassTest {
    public TestSetInheritanceWholeClass1() {
        super(List.of(
                new TestData(SetInheritanceWholeClassInput.class, SetInheritanceWholeClassOutput1.class, TestSetInheritanceWholeClass1.class),
                new TestData(SetInheritanceWholeClassInput.class, SetInheritanceWholeClassOutput2.class, TestSetInheritanceWholeClass2.class)
        ));
    }
}
