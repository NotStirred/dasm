package io.github.notstirred.dasm.api.redirect;

import io.github.notstirred.dasm.api.FieldSig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface AddFieldToSets {
    Class<?> owner();

    FieldSig field();

    Class<?>[] sets();
}
