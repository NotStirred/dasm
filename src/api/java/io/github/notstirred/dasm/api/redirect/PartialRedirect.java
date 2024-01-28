package io.github.notstirred.dasm.api.redirect;

import io.github.notstirred.dasm.api.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface PartialRedirect {
    Ref from();

    Ref to();
}
