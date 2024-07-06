package io.github.notstirred.dasm.util;

import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import static java.lang.Math.max;

@Data
public class TypeUtil {
    public static String classToDescriptor(Class<?> clazz) {
        return typeNameToDescriptor(clazz.getName());
    }

    public static String typeNameToDescriptor(String className) {
        return Method.getMethod(className.replace('/', '.') + " x()").getReturnType().getDescriptor();
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
