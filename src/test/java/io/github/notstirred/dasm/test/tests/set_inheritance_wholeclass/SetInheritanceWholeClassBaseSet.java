package io.github.notstirred.dasm.test.tests.set_inheritance_wholeclass;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.test.targets.Soup;

@RedirectSet
interface SetInheritanceWholeClassBaseSet {
    @TypeRedirect(from = @Ref(Soup.class), to = @Ref(String.class))
    abstract class SoupToStringRedirects { }
}
