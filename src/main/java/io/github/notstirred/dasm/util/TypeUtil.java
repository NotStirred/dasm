package io.github.notstirred.dasm.util;

import lombok.Data;
import org.objectweb.asm.Type;

import static java.lang.Math.max;

@Data
public class TypeUtil {
    public static String classToDescriptor(Class<?> clazz) {
        return classNameToDescriptor(clazz.getName());
    }

    public static String classNameToDescriptor(String className) {
        return "L" + className.replace('.', '/') + ";";
    }

    public static String classNameToInternalName(String className) {
        return className.replace('.', '/');
    }

    public static String classDescriptorToClassName(String descriptor) {
        return Type.getType(descriptor).getClassName();
    }

    public static String simpleClassNameOf(Type type) {
        String className = type.getClassName();
        return className.substring(max(0, className.lastIndexOf('.') + 1)).replace('$', '.');
    }
}
