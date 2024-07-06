package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * The annotated method <b><u>must</u></b> be within either a {@link TypeRedirect}, {@link InterOwnerContainer}, or {@link IntraOwnerContainer}, otherwise it will be ignored.
 * <br/><br/>
 * Specifies that uses of the annotation-specified method should be replaced with the annotated method.
 * <br/><br/>
 * By convention marked methods should be {@code native}, but {@code abstract} or a stub definition (preferably that throws any {@link RuntimeException}) are both equally valid.
 * <br/><br/>
 * <h2>Important notes</h2>
 * Changing the signature of a method is only valid with corresponding {@link TypeRedirect}s from the old->new types.
 * <br/><br/>
 * Changing the owner of a <b><u>non-static</u></b> method is only valid within a {@link TypeRedirect}.
 * <br/><br/>
 * <h2>Example:</h2>
 * <h3>E1:</h3>
 * Specifies a {@code private String} method with the name {@code existingMethodName}, and the new name {@code newMethodName}
 * <pre>{@code
 *     @MethodRedirect(@MethodSig(name = "existingMethodName", args = { }, ret = @Ref(String.class)))
 *     private native String newMethodName();
 * }</pre><br/>
 * <h3>E2:</h3>
 * Specifies a {@code private String} method with the name {@code existingMethodName}, and the new name {@code newMethodName}.<br/>
 * Additionally an optional mappings owner is specified, see: {@link MethodRedirect#mappingsOwner()}.
 * <pre>{@code
 *     @MethodRedirect(name = "existingMethodName", mappingsOwner = @Ref(OtherClass.class))
 *     private native String newMethodName();
 * }</pre>
 */
@Target(METHOD)
@Retention(CLASS)
public @interface MethodRedirect {
    /** The method to replace */
    MethodSig value();

    /**
     * The source method's mapping owner.
     * <br/><br/>
     * Only useful if the codebase is remapped and method/field owners are moved<br/>
     * Allows specifying the class which owns the method in the mappings
     */
    Ref mappingsOwner() default @Ref;
}
