package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface AddFieldToSets {
    Ref owner();

    Ref mappingsOwner() default @Ref;

    FieldSig field();

    Class<?>[] sets();
}
