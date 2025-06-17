package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Specifies that any occurrence of the type {@link #from()} must be replaced with the type {@link #to()}. Also provides
 * owner information to {@link io.github.notstirred.dasm.api.annotations.redirect.redirects inner redirects}.</p>
 * <p><b><u>Array types cannot be directly redirected</u></b>; instead the element type may be redirected.</p>
 * <p>By convention the marked class's name should have the format {@code FromClass_to_ToClass}, eg: {@code Object_to_String}.</p>
 *
 * <h2>Valid Redirects</h2>
 * <p>All redirects are valid within an {@link TypeRedirect}</p>
 * <p><i>For static owner-changing redirects see {@link InterOwnerContainer}, for non-owner-changing redirects see {@link IntraOwnerContainer}</i></p>
 *
 * <h2>Important Information</h2>
 * <ul>
 *   <li><b><u>The container type must match the {@link #to()} type in terms of {@code class}/{@code interface}</u></b>. ({@code abstract} doesn't matter).</li>
 *   <li>The container type must be within a {@link RedirectSet}.</li>
 * </ul>
 * <p>For information on containers and examples see {@link RedirectSet}</p>
 * <p>For more examples see {@link FieldRedirect}, {@link MethodRedirect}, {@link FieldToMethodRedirect}, {@link ConstructorToFactoryRedirect}</p>
 *
 * <h2>Example:</h2>
 * Specifies that {@code Object} should be redirected to {@code SomePrivateClass} (referred to by string, as it's private)
 * <pre>{@code
 *     @TypeRedirect(from = @Ref(Object.class), to = @Ref(string = "java.lang.SomePrivateClass"))
 *     abstract class Object_to_SomePrivateClass {
 *         // optionally redirects here
 *     }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface TypeRedirect {
    /**
     * The type to replace with {@link #to()}
     */
    Ref from();

    /**
     * The type to replace {@link #from()} with
     */
    Ref to();
}
