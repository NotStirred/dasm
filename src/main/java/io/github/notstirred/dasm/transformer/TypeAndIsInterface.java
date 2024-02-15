package io.github.notstirred.dasm.transformer;

import lombok.Value;
import org.objectweb.asm.Type;

@Value
public class TypeAndIsInterface {
    Type type;
    boolean isInterface;
}
