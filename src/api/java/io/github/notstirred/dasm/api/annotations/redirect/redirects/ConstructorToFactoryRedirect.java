package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * The annotated method <b><u>must</u></b> be within either a {@link TypeRedirect} <b><u>or</u></b> a {@link InterOwnerContainer}, otherwise it will be ignored.
 * <br/><br/>
 * Specifies that uses of the annotation-specified constructor should be replaced with the annotated factory method.
 * <br/><br/>
 * The method must have a typical factory signature, ie: {@code static Vec3i fromLong(long)}, but either be {@code native}, or throw.
 * <br/><br/>
 * <h2>Important notes</h2>
 * Changing the signature of a constructor is only valid with corresponding {@link TypeRedirect}s from the old->new types.
 * <br/><br/>
 * <h2>Example 1: Redirecting to a factory on the same class.</h2>
 * <ul>
 *   <li>Redirect usage of {@code new Vec3i(long)} to {@code Vec3i#fromLong(long))}</li>
 * </ul>
 * As the owner isn't changing ({@code Vec3i}->{@code Vec3i}) a {@link InterOwnerContainer} is used.
 * <pre>{@code
 * @IntraOwnerContainer(owner = @Ref(Vec3i.class))
 * abstract class Vec3i_redirects {
 *     @ConstructorToFactoryRedirect("<init>(J)V)
 *     static native Vec3i fromLong(long packed);
 * }
 * }</pre>
 * <h2>Example 2: Redirecting to a factory on a different class.</h2>
 * <ul>
 *   <li>Redirect usage of {@code new Vec3i(int, int, int)} to {@code Util#newVec3i(int, int, int))}</li>
 * </ul>
 * As the owner is changing ({@code Vec3i}->{@code Util}) <b><u>but</u></b> we don't want to {@link TypeRedirect} Vec3i to anything else a {@link InterOwnerContainer} is used.<br/>
 * This is <b><u>only valid for a static dst method</u></b> (the factory is static)
 * <pre>{@code
 * @InterOwnerContainer(owner = @Ref(Vec3i.class), newOwner = @Ref(Util.class))
 * abstract class Vec3i_redirects {
 *     @ConstructorToFactoryRedirect("<init>(III)V")
 *     static native Vec3i newVec3i(int x, int y, int z);
 * }
 * }</pre>
 * <h2>Example 3: Redirecting to a factory on the same class as a type redirect.</h2>
 * <ul>
 *   <li>Redirect usage of {@code new Vec3i(int, int, int)} to {@code Vec3d#of(double, double, double))}</li>
 * </ul>
 * As the owner is changing ({@code Vec3i}->{@code Vec3d}) <b><u>and</u></b> we want to replace all reference to {@code Vec3i} with {@code Vec3d}, a {@link TypeRedirect} is used.<br/>
 * Additionally a {@link TypeRedirect} from {@code int} -> {@code double} is <b><u>required</u></b> due to changing the signature.
 * <pre>{@code
 * @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(Vec3d.class))
 * abstract class Vec3i_redirects {
 *     @ConstructorToFactoryRedirect("<init>(III)V")
 *     static native Vec3d of(double x, double y, double z);
 * }
 *
 * @TypeRedirect(from = @Ref(int.class), to = @Ref(double.class))
 * abstract class int_to_double { }
 * }</pre>
 * <h2>Example 4: Redirecting to a factory on a different class than a type redirect.</h2>
 * <ul>
 *   <li>Redirect usage of {@code new Vec3i(int, int, int)} to {@code VecUtils#vec3dOf(double, double, double))}</li>
 *   <li>Redirect usage of {@code Vec3i} to {@code Vec3d}</li>
 * </ul>
 * In this example we want to replace all reference to {@code Vec3i} with {@code Vec3d}, so a {@link TypeRedirect} is used.<br/>
 * Additionally, a {@link InterOwnerContainer} is used to replace just the call to the constructor with a factory method <u>without</u> replacing other usages of {@code Vec3i} with {@code VecUtils}<br/>
 * Finally a {@link TypeRedirect} from {@code int} -> {@code double} is <b><u>required</u></b> due to changing the signature.
 * <br/><br/>
 * For additional info on redirect application order see {@link RedirectSet}'s redirect application order.
 * <pre>{@code
 * @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(Vec3d.class))
 * abstract class Vec3i_to_Vec3d { }
 *
 * @InterOwnerContainer(owner = @Ref(Vec3i.class), newOwner = @Ref(VecUtils.class))
 * abstract class Vec3i_to_VecUtils {
 *     @ConstructorToFactoryRedirect("<init>(III)V")
 *     static native Vec3d vec3dOf(double x, double y, double z);
 * }
 * }</pre>
 */
@Target(METHOD)
@Retention(CLASS)
public @interface ConstructorToFactoryRedirect {
    String value();
}
