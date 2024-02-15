package io.github.notstirred.dasm.transformer.data;

import lombok.Value;
import org.objectweb.asm.Type;

@Value
public class TypeAndIsInterface {
    Type type;
    boolean isInterface;
}
