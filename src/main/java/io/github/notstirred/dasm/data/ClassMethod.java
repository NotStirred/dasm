package io.github.notstirred.dasm.data;

import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

@Data
public class ClassMethod {
    public final Type owner;
    public final Type mappingOwner;
    public final Method method;
}
