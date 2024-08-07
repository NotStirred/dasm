package io.github.notstirred.dasm.api.annotations.selector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A concrete reference to a class through {@link Ref#value()} <b><u>or</u></b> a String through {@link Ref#string()}.
 * <br/><br/>
 * If both are specified {@link Ref#string()} is used.
 * <br/><br/>
 * <h2>Examples:</h2>
 * <p>Ex1:</p>
 * {@code @Ref(Object.class)}
 * <p>Ex2:</p>
 * {@code @Ref(AnOuterClass.AnInnerClass.class)}
 * <p>Ex3:</p>
 * {@code @Ref(string = "java.lang.String")}
 * <p>Ex4:</p>
 * {@code @Ref(string = "a.package.OuterClass$InnerClass")}
 */
@Target({ /* No targets allowed */ })
@Retention(RetentionPolicy.CLASS)
public @interface Ref {
    /**
     * The class to target
     */
    Class<?> value() default EmptyRef.class;

    /**
     * The string name of the class in the fully qualified name format, eg: "java.lang.Object"
     */
    String string() default "";

    class EmptyRef { }
}
