package io.github.notstirred.dasm.test.tests.integration.set_inheritance_wholeclass;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.test.targets.Soup;

@RedirectSet
interface SetInheritanceWholeClassDerivedSetB extends SetInheritanceWholeClassBaseSet {
    @TypeRedirect(from = @Ref(Soup.class), to = @Ref(Error.class))
    abstract class SoupToStringRedirects { }
}
