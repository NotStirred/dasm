package io.github.notstirred.dasm.api.annotations.transform;

import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface TransformFromMethod {
    MethodSig value();

    ApplicationStage stage() default ApplicationStage.PRE_APPLY;

    // FIXME: add synthetic accessors
//    boolean makeSyntheticAccessor() default false;

    Ref copyFrom() default @Ref();

    Class<?>[] useRedirectSets() default { };
}
