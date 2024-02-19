package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * <pre>{@code}</pre>
 * The annotated method <b><u>must</u></b> be within either a {@link TypeRedirect}, {@link InterOwnerContainer}, or {@link IntraOwnerContainer}, otherwise it will be ignored.
 * <br/><br/>
 * Specifies that uses of the annotation-specified field should be replaced with the annotated method.
 * <br/><br/>
 * The method must have a typical getter signature, ie: {@code int getX()}, but either be {@code native}, {@code abstract}, or throw.
 * <br/><br/>
 * <h2>Important notes</h2>
 * Changing the signature of a generated field->method is only valid with corresponding {@link TypeRedirect}s from the old->new types.
 * <br/><br/>
 * Changing the owner of a <b><u>non-static</u></b> field->method is only valid within a {@link TypeRedirect}.
 * <br/><br/>
 * <h2>Example 1:</h2>
 * <ul>
 *   <li>Redirect usage of {@code Vec3i#x} to {@code Vec3#getX()}/{@code Vec3#setX()}.</li>
 *   <li>Redirect usage of {@code Vec3i#z} to {@code Vec3#getZ()}/{@code Vec3#setZ()}.</li>
 * </ul>
 * {@code getX()} / {@code setX(double)} / {@code getZ()} / {@code setZ(double)} must exist and be accessible on Vec3.
 * <br/><br/>
 * As the owner is changing ({@code Vec3i}->{@code Vec3}) a {@link TypeRedirect} is used.<br/>
 * Additionally, a type redirect from {@code int}->{@code double} is required, as the {@link FieldToMethodRedirect}s change the return type.
 * <pre>{@code
 * @TypeRedirect(from = @Ref(Vec3i.class), to = @Ref(Vec3.class))
 * abstract class Vec3i_to_Vec3 {
 *     @FieldToMethodRedirect(@FieldSig(type = @Ref(int.class), name = "x"), setter = "setX")
 *     native double getX();
 *
 *     @FieldToMethodRedirect(@FieldSig(type = @Ref(int.class), name = "z"), setter = "setZ")
 *     native double getZ();
 * }
 * @TypeRedirect(from = @Ref(int.class), to = @Ref(double.class))
 * abstract class int_to_double { }
 * }</pre>
 * <br/>
 * <h2>Example 2:</h2>
 * <ul>
 *   <li>Redirect usage of {@code Vec3i#x} to {@code Vec3#getX()}.</li>
 *   <li>Redirect usage of {@code Vec3i#z} to {@code Vec3#getZ()}.</li>
 * </ul>
 * {@code getX()}/{@code getZ()} must exist and be accessible on Vec3i.
 * <br/><br/>
 * As the owner isn't changing ({@code Vec3i}->{@code Vec3i}) a {@link InterOwnerContainer} is used.
 * <pre>{@code
 * @IntraOwnerContainer(owner = @Ref(Vec3i.class))
 * abstract class Vec3i_redirects {
 *     @FieldToMethodRedirect(@FieldSig(type = @Ref(int.class), name = "x"))
 *     native int getX();
 *
 *     @FieldToMethodRedirect(@FieldSig(type = @Ref(int.class), name = "z"))
 *     native int getZ();
 * }
 * }</pre>
 */
@Target(METHOD)
@Retention(CLASS)
public @interface FieldToMethodRedirect {
    /** The field to replace access to. */
    FieldSig value();

    /**
     * The <b>name</b> of the setter method, must have the same owner (be in the same class) as the getter method
     * The argument will be inferred from the field and getter.
     */
    String setter() default "";

    /**
     * The source field's mapping owner.
     * <br/><br/>
     * Only useful if the codebase is remapped and method/field owners are moved<br/>
     * Allows specifying the class which owns the field in the mappings
     */
    Ref mappingsOwner() default @Ref;
}
