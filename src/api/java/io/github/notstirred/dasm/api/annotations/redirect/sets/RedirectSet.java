package io.github.notstirred.dasm.api.annotations.redirect.sets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Marks that an {@code interface} should be used as a redirect set.</p>
 * <p>By convention the marked type's name should end with `Set`</p>
 *
 * <h2>Containers</h2>
 * <p>A {@link RedirectSet} may contain any number of {@code interface}s, and/or {@code abstract class}es. These <b><u>must</u></b> be annotated with one of: {@link TypeRedirect}, {@link InterOwnerContainer}, or {@link IntraOwnerContainer}</p>
 *
 * <h2>Destination Type</h2>
 * A destination type is defined as "a type <i>after</i> dasm redirects are applied" eg:
 * <ul>
 *     <li>A {@link TypeRedirect}'s dst type is {@link TypeRedirect#to()}</li>
 *     <li>A {@link InterOwnerContainer}'s dst type is {@link InterOwnerContainer#to()}</li>
 *     <li>A {@link IntraOwnerContainer}'s dst type is {@link IntraOwnerContainer#value()}</li>
 * </ul>
 *
 * <h2>Redirect Set Inheritance</h2>
 * A {@link RedirectSet} may inherit from one or more {@link RedirectSet}s using java's {@code extends} syntax as normal.
 * This results in a derived type inheriting all the redirects from the super type(s).<br/>
 * <ul>
 *   <li>Redirects from an inherited {@link RedirectSet} can be overridden by redefining them within the derived type.</li>
 *   <li>If {@link RedirectSet} {@code A extends B, C} the redirects from {@code C} will override those in {@code B};
 *       the resulting redirects will then be overridden by those in {@code A}
 *   </li>
 * </ul>
 *
 * <h2>Redirect Chaining</h2>
 * All redirects within a {@link RedirectSet} (including all inheritance) apply as a single atomic step. For example if
 * {@code A} is redirected to {@code B}, and {@code B} to {@code A} the result will <b>always</b> be A and B swapping
 * and will <b>never</b> result in all As or all Bs
 *
 * <h2>Example</h2>
 * <pre>{@code
 * @RedirectSet
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
