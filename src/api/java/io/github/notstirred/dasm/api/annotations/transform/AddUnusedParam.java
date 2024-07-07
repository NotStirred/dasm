package io.github.notstirred.dasm.api.annotations.transform;

import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.CLASS)
@Repeatable(AddUnusedParam.List.class)
public @interface AddUnusedParam {
    Ref type();

    int index();

    /**
     * A wrapper annotation that makes the {@link AddUnusedParam} annotation repeatable.
     *
     * <p>Programmers generally do not need to write this. It is created by Java when a programmer
     * writes more than one {@link AddUnusedParam} annotation at the same location.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
    @interface List {
        /**
         * Return the repeatable annotations.
         *
         * @return the repeatable annotations
         */
        AddUnusedParam[] value();
    }
}
