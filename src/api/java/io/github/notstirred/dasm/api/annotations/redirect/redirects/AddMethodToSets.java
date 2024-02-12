package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AddMethodToSets {
    Ref owner();

    boolean ownerIsInterface();

    MethodSig method();

    Class<?>[] sets();

    Ref mappingsOwner() default @Ref;
}
