package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

public @interface FieldToMethodRedirect {
    FieldSig value();

    /** The name of the setter method, must have the same owner (be in the same class) as the getter method */
    String setter() default "";

    Ref mappingsOwner() default @Ref;
}
