package io.github.notstirred.dasm.api.annotations.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface TransformClass {
    Class<?> sets();

    ApplicationStage stage() default ApplicationStage.PRE_APPLY;
}
