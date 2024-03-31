package io.github.notstirred.dasm.api.annotations.transform;

import io.github.notstirred.dasm.api.annotations.selector.MethodSig;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

@Target({METHOD, CONSTRUCTOR})
@Retention(CLASS)
public @interface TransformMethod {
    MethodSig value();

    ApplicationStage stage() default ApplicationStage.PRE_APPLY;

    // FIXME: add synthetic accessors
//    boolean makeSyntheticAccessor() default false;

    Class<?>[] useRedirectSets() default {};
}
