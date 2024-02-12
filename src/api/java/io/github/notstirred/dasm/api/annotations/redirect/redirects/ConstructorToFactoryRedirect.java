package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target(METHOD)
@Retention(CLASS)
public @interface ConstructorToFactoryRedirect {
    ConstructorMethodSig value();
}
