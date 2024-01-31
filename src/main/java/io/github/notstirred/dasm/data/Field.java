package io.github.notstirred.dasm.data;

import lombok.Data;
import org.objectweb.asm.Type;

@Data
public class Field {
    public final Type type;
    public final String name;
}
