package io.github.notstirred.dasm.api.annotations.redirect.redirects;

import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectContainer;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>{@code}</pre>
 * <p>
 * Must be marked on any method within a class marked with {@link TypeRedirect}, {@link RedirectContainer}.<br/>
 * The signature of the method marked species the method to target within the {@link TypeRedirect#from()} class.<br/>
 * By convention marked methods should be {@code native}, but {@code abstract} or a stub definition (preferably that throws any {@link RuntimeException}) are equally valid.
 * </p><br/>
 * <h2>Example:</h2>
 * <h3>E1:</h3>
 * Specifies a {@code private String} method with the name {@code existingMethodName}, and the new name {@code newMethodName}
 * <pre>{@code
 *     @MethodRedirect(ret = name = "existingMethodName")
 *     private native String newMethodName();
 * }</pre><br/>
 * <h3>E2:</h3>
 * Specifies a {@code private String} method with the name {@code existingMethodName}, and the new name {@code newMethodName}.<br/>
 * Additionally an optional mappings owner is specified, see: {@link MethodRedirect#mappingsOwner()}.
 * <pre>{@code
 *     @MethodRedirect(ret = name = "existingMethodName", mappingsOwner = @Ref(OtherClass.class))
 *     private native String newMethodName();
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface MethodRedirect {
    Ref ret();

    String name();

    Ref[] args();

    /**
     * Only useful if the codebase is remapped and methods owners are moved
     * Allows specifying the class which owns the method in the mappings
     */
    Ref mappingsOwner() default @Ref;
}
