package io.github.notstirred.dasm.test.tests.setinheritance;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

@RedirectSet
interface TestSetInheritanceDerivedSetB extends TestSetInheritanceBaseSet {
    @TypeRedirect(from = @Ref(Object.class), to = @Ref(Error.class))
    abstract class ObjectToStringRedirects { }
}
