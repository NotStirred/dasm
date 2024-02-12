package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface AddTransformToSets {
    Class<?>[] value();
}
