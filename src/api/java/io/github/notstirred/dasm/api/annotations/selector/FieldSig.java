package io.github.notstirred.dasm.api.annotations.selector;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A reference to a field.
 * <br/><br/>
 * <h2>Examples</h2>
 * <pre>{@code
 * @FieldSig(type = @Ref(String.class), name = "address")
 *
 * @FieldSig(type = @Ref(byte[].class), name = "data")
 *
 * @FieldSig(type = @Ref(string = "com.example.APrivateClass"), name = "secret")
 * }</pre>
 */
@Target({/* No targets allowed */})
@Retention(RetentionPolicy.CLASS)
public @interface FieldSig {
    /** The type of the field. */
    Ref type();

    /** The name of the field. */
    String name();
}
