package io.github.notstirred.dasm.api.annotations.selector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({/* No targets allowed */})
@Retention(RetentionPolicy.CLASS)
public @interface MethodSig {
    @Deprecated String value() default "";

    Ref ret() default @Ref(void.class);

    String name() default "";

    Ref[] args() default { };
}
