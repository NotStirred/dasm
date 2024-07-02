package io.github.notstirred.dasm.transformer.data;

import lombok.Value;
import org.objectweb.asm.Type;

@Value
public class AddedParameter {
    Type type;
    String name;
    /**
     * The index of the parameter in its method definition
     */
    int index;
}
