package io.github.notstirred.dasm.util;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.StringJoiner;

public class Format {
    private static final int PACKAGE_LENGTH = 1;

    private static String formatType(Type type) {
        StringJoiner joiner = new StringJoiner(".");
        String[] split = type.getClassName().split("\\.");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (i == split.length - 1)
                joiner.add(s);
            else
                joiner.add(s.substring(0, Math.min(PACKAGE_LENGTH, s.length())));
        }
        return joiner.toString();
    }

    public static String format(FieldNode fieldNode) {
        return formatType(Type.getType(fieldNode.desc)) + " " + fieldNode.name;
    }

    public static String format(MethodNode methodNode) {
        StringBuilder s = new StringBuilder();
        Type methodType = Type.getMethodType(methodNode.desc);
        s.append(methodNode.name).append("(");
        StringJoiner j = new StringJoiner(" ");
        for (Type argumentType : methodType.getArgumentTypes()) {
            j.add(formatType(argumentType));
        }
        s.append(j);
        s.append(")");
        s.append(formatType(methodType.getReturnType()));

        return s.toString();
    }

    public static String format(ClassNode classNode) {
        return formatType(Type.getObjectType(classNode.name));
    }
}
