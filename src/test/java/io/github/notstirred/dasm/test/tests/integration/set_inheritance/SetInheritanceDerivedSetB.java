package io.github.notstirred.dasm.test.tests.integration.set_inheritance;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

@RedirectSet
interface SetInheritanceDerivedSetB extends SetInheritanceBaseSet {
    @TypeRedirect(from = @Ref(Object.class), to = @Ref(Error.class))
    abstract class ObjectToStringRedirects { }
}
