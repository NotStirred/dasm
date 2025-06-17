package io.github.notstirred.dasm.api.annotations.redirect.sets;

import io.github.notstirred.dasm.api.annotations.redirect.redirects.*;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>Does not redirect anything itself, but provides owner information to
 * {@link io.github.notstirred.dasm.api.annotations.redirect.redirects inner redirects}.</p>
 *
 * <p>By convention the marked class's name should have the format {@code ValueClass_redirects}, eg: {@code String_redirects}.</p>
 *
 * <h2>Valid Redirects</h2>
 * <p>All non-owner-changing redirects are valid within an {@link IntraOwnerContainer}</p>
 * <p><i>For owner-changing redirects see {@link TypeRedirect}, for static owner-changing redirects see {@link InterOwnerContainer}</i></p>
 *
 * <h2>Important Information</h2>
 * <ul>
 *   <li><b><u>The container type must match the {@link #value()} type in terms of {@code class}/{@code interface}</u></b>. ({@code abstract} doesn't matter).</li>
 *   <li>The container type must be within a {@link RedirectSet}.</li>
 * </ul>
 * <p>For information on containers and examples see {@link RedirectSet}</p>
 * <p>For more examples see {@link FieldRedirect}, {@link MethodRedirect}, {@link FieldToMethodRedirect}, {@link ConstructorToFactoryRedirect}</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface IntraOwnerContainer {
    Ref value();
}
