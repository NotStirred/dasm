package io.github.notstirred.dasm.api.annotations.transform;

import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
public @interface TransformFrom {
    MethodSig value();

    ApplicationStage stage() default ApplicationStage.PRE_APPLY;

    boolean makeSyntheticAccessor() default false;

    Ref copyFrom() default @Ref();

    /**
     * <h2>Do <u><b>NOT</b></u> confuse with {@link TransformFrom#addToRedirectSets}</h2>
     */
    Class<?>[] useRedirectSets() default { };

    /**
     * <h2>Do <u><b>NOT</b></u> confuse with {@link TransformFrom#useRedirectSets}</h2>
     */
    Class<?>[] addToRedirectSets() default { };

    enum ApplicationStage {
        PRE_APPLY,
        POST_APPLY
    }
}
