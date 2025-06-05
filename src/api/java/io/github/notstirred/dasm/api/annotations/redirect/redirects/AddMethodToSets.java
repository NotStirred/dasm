package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * May be added to a method within a {@link Dasm} class.
 * <br/><br/>
 * Add a {@link MethodRedirect} to multiple containers inline at method definition, rather than duplicating the method
 * definition inside a containers.
 * <br/><br/>
 * <h2>Important notes:</h2>
 * See {@link MethodRedirect}'s important notes for more information.
 * <h2>Example:</h2>
 * This adds a {@link MethodRedirect} from {@code Bar.getBarX()} -> {@code Foo.getX()} to {@code Bar_to_Foo_redirects} which is a {@link TypeRedirect}
 * <pre>{@code
 * @Dasm
 * public class Foo {
 *     @AddMethodToSets(
 *         containers = Bar_to_Foo_redirects
 *         method = @MethodSig(name = "getBarX", ret = int.class, args = { }),
 *     )
 *     private int getX();
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AddMethodToSets {
    /**
     * The containers to add the {@link MethodRedirect} to.
     * <p>Valid container classes are {@link TypeRedirect}, {@link InterOwnerContainer}, {@link IntraOwnerContainer}</p>
     */
    Class<?>[] containers();

    /**
     * The method to replace
     */
    MethodSig method();

    /**
     * The source method's mapping owner.
     * <br/><br/>
     * Only useful if the codebase is remapped and method/field owners are moved<br/>
     * Allows specifying the class which owns the method in the mappings
     */
    Ref mappingsOwner() default @Ref;
}
