package io.github.notstirred.dasm.test.tests.setinheritance;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

@RedirectSet
interface TestSetInheritanceBaseSet {
    @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
    abstract class ObjectToStringRedirects { }
}
