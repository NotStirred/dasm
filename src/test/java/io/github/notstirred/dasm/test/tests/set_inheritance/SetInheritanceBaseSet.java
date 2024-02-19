package io.github.notstirred.dasm.test.tests.set_inheritance;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

@RedirectSet
interface SetInheritanceBaseSet {
    @TypeRedirect(from = @Ref(Object.class), to = @Ref(String.class))
    abstract class ObjectToStringRedirects { }
}
