package io.github.notstirred.dasm.data;

import io.github.notstirred.dasm.api.provider.MappingsProvider;
import lombok.Data;
import lombok.NonNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

@Data
public class ClassMethod {
    private final Type owner;
    private final Type mappingOwner;
    private final Method method;

    public ClassMethod(Type owner, @NonNull Type mappingOwner, Method method) {
        this.owner = owner;
        this.mappingOwner = mappingOwner;
        this.method = method;
    }

    public ClassMethod(Type owner, Method method) {
        this.owner = owner;
        this.mappingOwner = owner;
        this.method = method;
    }

    public ClassMethod remap(MappingsProvider mappingsProvider) {
        Type[] params = Type.getArgumentTypes(this.method.getDescriptor());
        Type returnType = Type.getReturnType(this.method.getDescriptor());

        Type mappedType = mappingsProvider.remapType(this.owner);
        String mappedName = mappingsProvider.mapMethodName(this.mappingOwner.getClassName(), this.method.getName(), this.method.getDescriptor());
        Type[] mappedParams = new Type[params.length];
        for (int i = 0; i < params.length; i++) {
            mappedParams[i] = mappingsProvider.remapDescType(params[i]);
        }
        Type mappedReturnType = mappingsProvider.remapDescType(returnType);
        return new ClassMethod(mappedType, mappedType, new Method(mappedName, mappedReturnType, mappedParams));
    }
}
