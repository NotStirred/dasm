package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>{@code}</pre>
 * May be added to a method within a {@link Dasm} class.
 * <br/><br/>
 * Add a {@link MethodRedirect} to multiple {@link RedirectSet}s inline at method definition, rather than duplicating the method definition inside a {@link RedirectSet}.
 * <br/><br/>
 * <h2>Important notes:</h2>
 * Changing the signature of a method is only valid with corresponding {@link TypeRedirect}s from the old->new types.
 * <br/><br/>
 * Changing the owner of a <b><u>non-static</u></b> method is only valid with a corresponding {@link TypeRedirect}.<br/>
 * In almost all cases with {@link AddMethodToSets} this means having a {@link TypeRedirect} from the {@link AddMethodToSets#owner} to the owner of the annotated method.
 * <br/><br/>
 * <h2>Example:</h2>
 * This redirects {@code Bar.getBarX()} -> {@code Foo.getX()}. Due to the owner change a {@link TypeRedirect} from {@code Bar} to {@code Foo} is <b><u>required</u></b>.
 * <pre>{@code
 * @Dasm
 * public class Foo {
 *     @AddMethodToSets(
 *         owner = @Ref(Bar.class),
 *         method = @MethodSig(name = "getBarX", ret = int.class, args = { }),
 *         sets = SomeSet.class
 *     )
 *     private int getX();
 * }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AddMethodToSets {
    /**
     * The class that has the method to replace as a member method.
     * <br/><br/>
     * For example: {@link String#hashCode()}, {@link String} is the owner.
     */
    Ref owner();

    /**
     * The source method's mapping owner.
     * <br/><br/>
     * Only useful if the codebase is remapped and method/field owners are moved<br/>
     * Allows specifying the class which owns the method in the mappings
     */
    Ref mappingsOwner() default @Ref;

    /** The method to replace */
    MethodSig method();

    /** The {@link RedirectSet}s to add the {@link MethodRedirect} to. */
    Class<?>[] sets();
}
