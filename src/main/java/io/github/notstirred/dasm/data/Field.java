package io.github.notstirred.dasm.data;

import lombok.Data;
import org.objectweb.asm.Type;

@Data
public class Field {
    private final Type type;
    private final String name;
}
