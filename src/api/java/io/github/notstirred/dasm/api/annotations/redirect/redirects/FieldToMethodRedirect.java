package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

public @interface FieldToMethodRedirect {
    Ref fieldOwner();

    FieldSig field();

    Ref methodMappingsOwner() default @Ref;
}
