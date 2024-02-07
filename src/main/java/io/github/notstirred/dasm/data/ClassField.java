package io.github.notstirred.dasm.data;

import io.github.notstirred.dasm.api.provider.MappingsProvider;
import lombok.Data;
import org.objectweb.asm.Type;

@Data
public class ClassField {
    private final Type owner;

    /**
     * The owner to be used when remapping the method using the {@link MappingsProvider}
     */
    private final Type mappingsOwner;
    private final Type type;
    private final String name;

    public ClassField remap(MappingsProvider mappingsProvider) {
        Type mappedType = mappingsProvider.remapType(this.owner);
        String mappedName = mappingsProvider.mapFieldName(this.owner.getClassName(), this.name, this.type.getDescriptor());
        Type mappedDesc = mappingsProvider.remapDescType(this.type);
        return new ClassField(mappedType, mappedType, mappedDesc, mappedName);
    }
}
