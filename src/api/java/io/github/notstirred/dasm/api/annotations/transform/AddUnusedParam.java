package io.github.notstirred.dasm.api.annotations.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be added to a parameter in a method which is annotated with {@link TransformMethod} or {@link TransformFromMethod}
 * <br/><br/>
 * Any number of parameters can be added in any position, but the original parameters must exist somewhere in the
 * signature, with their order unchanged.
 * <br/><br/>
 * <h2>Examples:</h2>
 * <pre>{@code
 * Here a string is inserted between the two original integers in the method signature.
 * @TransformFromMethod(value = @MethodSig("method1(II)V"))
 * public native float[] method1out1(int a, @AddUnusedParam String c, int b);
 * }</pre>
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.CLASS)
public @interface AddUnusedParam {
}
