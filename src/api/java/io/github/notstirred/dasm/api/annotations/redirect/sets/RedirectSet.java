package io.github.notstirred.dasm.api.annotations.redirect.sets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>{@code}</pre>
 * Marks that an {@code interface} should be used as a redirect set.
 * <br/><br/>
 * The marked type must be an {@code interface} and must contain only {@code abstract class} definitions marked with {@link TypeRedirect}<br/>
 * By convention the marked type's name should end with `RedirectSet`, or `Set`
 * <br/><br/>
 * <h2>Example</h2>
 * <pre>{@code
 * @DasmRedirectSet
 * interface ExampleSet {
 *
 *     @TypeRedirect(from = @Ref(Foo.class), to = @Ref(Bar.class))
 *     abstract class FooToBarRedirects {
 *
 *         @FieldRedirect("newName") public String stringThing;
 *     }
 * }
 * }</pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface RedirectSet {
}
