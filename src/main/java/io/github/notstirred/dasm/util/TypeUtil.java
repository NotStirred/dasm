package io.github.notstirred.dasm.util;

import lombok.Data;
import org.objectweb.asm.Type;

@Data
public class TypeUtil {
    public static String classToDescriptor(Class<?> clazz) {
        return classNameToDescriptor(clazz.getName());
    }

    public static String classNameToDescriptor(String className) {
        return "L" + className.replace('.', '/') + ";";
    }

    public static String classDescriptorToClassName(String descriptor) {
        return Type.getType(descriptor).getClassName();
    }
}
