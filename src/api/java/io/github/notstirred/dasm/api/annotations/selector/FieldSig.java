package io.github.notstirred.dasm.api.annotations.selector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({/* No targets allowed */})
@Retention(RetentionPolicy.CLASS)
public @interface FieldSig {
    Class<?> type();
    String name();
}
