package io.github.notstirred.dasm.api.annotations.redirect.sets;

import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RedirectContainer {
    Ref owner();

    /** If not specified, the value provided to {@link RedirectContainer#owner()} is used (ie no owner change). */
    Ref newOwner() default @Ref;
}
