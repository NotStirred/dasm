package io.github.notstirred.dasm.util;

import io.github.notstirred.dasm.data.Field;
import io.github.notstirred.dasm.exception.DasmException;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

public class ReferenceUtil {
    public static Field parseFieldReference(String reference) throws InvalidReference {
        String[] split = reference.split(":");

        if (split.length != 2 || split[0].isEmpty() || split[1].isEmpty()) {
            throw new InvalidReference(reference);
        }

        return new Field(Type.getType(split[1]), split[0]);
    }

    public static Method parseMethodReference(String reference) throws InvalidReference {
        if (reference.equals("<clinit>")) {
            return new Method("<clinit>", "()V");
        }

        int separator = reference.indexOf('(');

        if (separator == -1) {
            throw new InvalidReference(reference);
        }

        String name = reference.substring(0, separator);
        String descriptor = reference.substring(separator);

        if (name.isEmpty() || descriptor.isEmpty()) {
            throw new InvalidReference(reference);
        }

        return new Method(name, descriptor);
    }

    public static class InvalidReference extends DasmException {
        public InvalidReference(String reference) {
            super("Invalid reference: " + reference);
        }
    }
}
