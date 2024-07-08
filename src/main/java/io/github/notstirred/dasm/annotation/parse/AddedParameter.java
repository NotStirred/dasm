package io.github.notstirred.dasm.annotation.parse;

import lombok.Value;
import org.objectweb.asm.Type;

@Value
public class AddedParameter {
    Type type;
    int index;
}
