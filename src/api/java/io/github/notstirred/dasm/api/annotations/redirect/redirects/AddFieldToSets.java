package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * May be added to a field within a {@link Dasm} class.
 * <br/><br/>
 * Add a {@link FieldRedirect} to multiple containers inline at field definition, rather than duplicating the field
 * definition inside the containers.
 * <br/><br/>
 * <h2>Important notes:</h2>
 * See {@link FieldRedirect}'s important notes for more information.
 * <h2>Example:</h2>
 * This adds a {@link FieldRedirect} from {@code barX} -> {@code x} to {@code Bar_to_Foo_redirects} which is a {@link TypeRedirect}
 * <pre>{@code
 * @Dasm
 * public class Foo {
 *     @AddFieldToSets(
 *         containers = Bar_to_Foo_redirects.class,
 *         field = "barX:I",
 *     )
 *     private final int x;
 * }
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface AddFieldToSets {
    /**
     * The containers to add the {@link FieldRedirect} to.
     * <p>Valid container classes are {@link TypeRedirect}, {@link InterOwnerContainer}, {@link IntraOwnerContainer}</p>
     */
    Class<?>[] containers();

    /**
     * The field to replace
     */
    String field();

    /**
     * The source field's mapping owner.
     * <br/><br/>
     * Only useful if the codebase is remapped and method/field owners are moved<br/>
     * Allows specifying the class which owns the field in the mappings
     */
    Ref mappingsOwner() default @Ref;
}
