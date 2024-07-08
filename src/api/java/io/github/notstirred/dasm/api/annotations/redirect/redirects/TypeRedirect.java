package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An element of a {@link RedirectSet}
 * <br/><br/>
 * Specifies that any occurrence of the type {@link #from()} must be replaced with the type {@link #to()}.<br/>
 * <b><u>Array types cannot be directly redirected</u></b>; instead the element type may be redirected.
 * <br/><br/>
 * Must be marked on a type within a {@link RedirectSet} {@code interface}.
 * <br/><br/>
 * The marked type <b><u>must</u></b> match the {@link #to()} type in terms of {@code class}/{@code interface}, ({@code abstract} doesn't matter).
 * <br/><br/>
 * The type marked with {@link TypeRedirect} can optionally contain any number of {@link FieldRedirect}s and {@link MethodRedirect}
 *
 * <br/><br/>
 * By convention the marked class's name should have the format {@code FromClass_to_ToClass}, eg: {@code Object_to_String}.
 * It should also be {@code abstract}
 * <br/><br/>
 * <h2>Example:</h2>
 * Specifies that {@code Object} should be redirected to {@code SomePrivateClass} (referred to by string, as it's private)
 * <pre>{@code
 *     @TypeRedirect(from = @Ref(Object.class), to = @Ref(string = "java.lang.SomePrivateClass"))
 *     abstract class Object_to_SomePrivateClass {
 *         // optionally field and method redirects here
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
