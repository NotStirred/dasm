package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectContainer;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>{@code}</pre>
 * Must be marked on any field within a class marked with {@link TypeRedirect} or {@link RedirectContainer}.<br/>
 * The {@link FieldRedirect#value()} specifies the field to target within the source class.
 * The type, name, and access of the field specify what to redirect to within the destination class.
 * <br/><br/>
 * The {@code final} keyword must be omitted to avoid having to define a constructor to set it.
 * <p/>
 * <h2>Example:</h2>
 * The following field redirect specifies a redirect from {@code int existingFieldName} to {@code int newFieldName}
 * <pre>{@code
 *     @FieldRedirect(type = @Ref(int.class), name = "existingFieldName")
 *     private int newFieldName;
 * }</pre>
 * <p/>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface FieldRedirect {
    FieldSig value();

    /**
     * Only useful if the codebase is remapped and field owners are moved
     * Allows specifying the class which owns the field in the mappings
     */
    Ref mappingsOwner() default @Ref;
}
