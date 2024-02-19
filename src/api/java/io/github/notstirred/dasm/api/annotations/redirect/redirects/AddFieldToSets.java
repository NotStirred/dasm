package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>{@code}</pre>
 * May be added to a field within a {@link Dasm} class.
 * <br/><br/>
 * Add a {@link FieldRedirect} to multiple {@link RedirectSet}s inline at field definition, rather than duplicating the field definition inside a {@link RedirectSet}.
 * <br/><br/>
 * <h2>Important notes:</h2>
 * Changing the type of a field is only valid with a corresponding {@link TypeRedirect} from the old->new type.
 * <br/><br/>
 * Changing the owner of a <b><u>non-static</u></b> field is only valid with a corresponding {@link TypeRedirect}.<br/>
 * In almost all cases with {@link AddFieldToSets} this means having a {@link TypeRedirect} from the {@link AddFieldToSets#owner} to the owner of the annotated field.
 * <br/><br/>
 * <h2>Example:</h2>
 * This redirects {@code Bar.barX} -> {@code Foo.x}. Due to the owner change a {@link TypeRedirect} from {@code Bar} to {@code Foo} is <b><u>required</u></b>.
 * <pre>{@code
 * @Dasm
 * public class Foo {
 *     @AddFieldToSets(
 *         owner = @Ref(Bar.class),
 *         field = @FieldSig(type = int.class, name = "barX"),
 *         sets = SomeSet.class
 *     )
 *     private final int x;
 * }
 * }</pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface AddFieldToSets {
    /**
     * The class that has the field to replace as a member field.
     * <br/><br/>
     * For example: {@link String}'s hash field, {@link String} is the owner.
     */
    Ref owner();

    /**
     * The source field's mapping owner.
     * <br/><br/>
     * Only useful if the codebase is remapped and method/field owners are moved<br/>
     * Allows specifying the class which owns the field in the mappings
     */
    Ref mappingsOwner() default @Ref;

    /** The field to replace */
    FieldSig field();

    /** The {@link RedirectSet}s to add the {@link FieldRedirect} to. */
    Class<?>[] sets();
}
