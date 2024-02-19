package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>{@code}</pre>
 * May be added to a method within a {@link Dasm} class. The annotated item <b><u>must</u></b> be annotated with {@link TransformFromMethod} or {@link TransformFromClass}
 * <br/><br/>
 * Add a redirect from the source for the transform, to the resulting copied method, rather than duplicating the method definition inside a {@link RedirectSet}.
 * <ul>
 *  <li>In the case of {@link TransformFromMethod}, adds a {@link MethodRedirect}</li>
 *  <li>In the case of {@link TransformFromClass}, adds a {@link TypeRedirect}</li>
 * </ul>
 */
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.TYPE })
@Retention(RetentionPolicy.CLASS)
public @interface AddTransformToSets {
    /** The {@link RedirectSet}s to add the {@link MethodRedirect} or {@link TypeRedirect} to. */
    Class<?>[] value();
}
