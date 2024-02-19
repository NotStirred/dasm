package io.github.notstirred.dasm.api.annotations.redirect.sets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Does not redirect anything itself, but provides owner information to inner redirects
 * <br/><br/>
 * May not contain non-static redirects. For non-static redirects see {@link TypeRedirect}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface InterOwnerContainer {
    Ref owner();

    Ref newOwner();
}
