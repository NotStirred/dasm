package io.github.notstirred.dasm.api.annotations.redirect.sets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that an {@code interface} should be used as a redirect set.
 * <br/><br/>
 * The marked type must be an {@code interface} and must contain only {@code abstract class} definitions marked with {@link TypeRedirect}<br/>
 * By convention the marked type's name should end with `RedirectSet`, or `Set`
 * <br/><br/>
 * A {@link RedirectSet} may contain any number of {@code interface}s, and/or {@code abstract class}es. That <b><i>must</i></b> be annotated with one of: {@link TypeRedirect}, {@link InterOwnerContainer}, or {@link IntraOwnerContainer}<br/>
 * <ul>
 *     <li>An inner type must be an {@code interface} if its destination type is an {@code interface}</li>
 *     <li>An inner type must be an {@code abstract class} if its destination type is an {@code abstract class}</li>
 * </ul>
 * <h2>Destination Type</h2>
 * A destination type is defined as a type <i>after</i> dasm redirects are applied eg:
 * <ul>
 *     <li>A {@link TypeRedirect}'s dst type is {@link TypeRedirect#to()}</li>
 *     <li>A {@link InterOwnerContainer}'s dst type is {@link InterOwnerContainer#to()}</li>
 *     <li>A {@link IntraOwnerContainer}'s dst type is {@link IntraOwnerContainer#value()}</li>
 * </ul>
 * <p>
 * <br/><br/>
 * <h2>Example</h2>
 * <pre>{@code
 * @DasmRedirectSet
 * interface ExampleSet {
 *     @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
 *     abstract class FooToBarRedirects {
 *         @FieldRedirect("newName") public String stringThing;
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RedirectSet {
}
