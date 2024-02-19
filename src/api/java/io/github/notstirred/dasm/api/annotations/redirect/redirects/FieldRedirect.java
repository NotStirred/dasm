package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>{@code}</pre>
 * The annotated field <b><u>must</u></b> be within either a {@link TypeRedirect}, {@link InterOwnerContainer}, or {@link IntraOwnerContainer}, otherwise it will be ignored.
 * <br/><br/>
 * Specifies that uses of the annotation-specified field should be replaced with the annotated field.
 * <br/><br/>
 * <h2>Important notes</h2>
 * Changing the type of a field is only valid with a corresponding {@link TypeRedirect} from the old->new type.
 * <br/><br/>
 * Changing the owner of a <b><u>non-static</u></b> field is only valid within a {@link TypeRedirect}.
 * <br/><br/>
 * <h2>Example:</h2>
 * The following field redirect specifies a redirect from {@code int existingFieldName} to {@code int newFieldName}
 * <pre>{@code
 *     @FieldRedirect(type = @Ref(int.class), name = "existingFieldName")
 *     private int newFieldName;
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface FieldRedirect {
    /** The field to replace */
    FieldSig value();

    /**
     * The source field's mapping owner.
     * <br/><br/>
     * Only useful if the codebase is remapped and method/field owners are moved<br/>
     * Allows specifying the class which owns the field in the mappings
     */
    Ref mappingsOwner() default @Ref;
}
