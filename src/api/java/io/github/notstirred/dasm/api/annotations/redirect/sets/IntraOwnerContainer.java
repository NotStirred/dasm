package io.github.notstirred.dasm.api.annotations.redirect.sets;

import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Does not redirect anything itself, but provides owner information to inner redirects
 * <h2>Valid Redirects</h2>
 * <ul>
 *   <li>All redirects are valid within an {@link IntraOwnerContainer}</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface IntraOwnerContainer {
    Ref value();
}
