package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * May be added to a field within a {@link Dasm} class.
 * <br/><br/>
 * Add a {@link FieldToMethodRedirect} to multiple containers inline at method definition, rather than duplicating the
 * method definition inside the containers.
 * <br/><br/>
 * <h2>Important notes:</h2>
 * See {@link FieldToMethodRedirect}'s important notes for more information.
 * <h2>Example:</h2>
 * This adds a {@link FieldToMethodRedirect} from {@code barX} -> {@code int getBarX()} and {@code void setBarX(int)} to {@code Bar_redirects} which is an {@link IntraOwnerContainer}
 * <pre>{@code
 * @Dasm
 * public class Foo {
 *     private int foo = 0;
 *
 *     @AddFieldToMethodToSets(
 *         containers = Bar_redirects.class,
 *         field = @FieldSig(type = int.class, name = "barX"),
 *         setter = "setBarX"
 *     )
 *     public int getBarX() {
 *         return this.foo; // This method gets called for every replaced read
 *     }
 *     public void setBarX(int barX) {
 *         this.foo = barX; // This method gets called for every replaced write
 *     }
 * }
 * }</pre>
 */
@Target(METHOD)
@Retention(CLASS)
public @interface AddFieldToMethodToSets {
    /**
     * The containers to add the {@link FieldToMethodRedirect} to.
     * <p>Valid container classes are {@link TypeRedirect}, {@link InterOwnerContainer}, {@link IntraOwnerContainer}</p>
     */
    Class<?>[] containers();

    /**
     * The field to replace
     */
    FieldSig field();

    /**
     * The <b>name</b> of the setter method, must have the same owner (be in the same class) as the getter method
     * The argument will be inferred from the field and getter.
     */
    String setter() default "";

    /**
     * The source field's mapping owner.
     * <br/><br/>
     * Only useful if the codebase is remapped and method/field owners are moved<br/>
     * Allows specifying the class which owns the field in the mappings
     */
    Ref mappingsOwner() default @Ref;
}
