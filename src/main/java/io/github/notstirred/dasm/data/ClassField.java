package io.github.notstirred.dasm.data;

import io.github.notstirred.dasm.api.provider.MappingsProvider;
import lombok.Data;
import org.objectweb.asm.Type;

@Data
public class ClassField {
    public final Type owner;

    /**
     * The owner to be used when remapping the method using the {@link MappingsProvider}
     */
    public final Type mappingsOwner;
    public final Type type;
    public final String name;
}
