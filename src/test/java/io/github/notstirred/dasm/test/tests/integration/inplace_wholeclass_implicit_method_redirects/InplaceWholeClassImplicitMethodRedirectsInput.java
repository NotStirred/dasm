package io.github.notstirred.dasm.test.tests.integration.inplace_wholeclass_implicit_method_redirects;

import io.github.notstirred.dasm.api.annotations.transform.TransformClass;

@TransformClass(sets = TestInplaceWholeClassImplicitMethodRedirects.Set.class)
public class InplaceWholeClassImplicitMethodRedirectsInput {
    void method1(Float a) {
        a.hashCode();
    }
}
