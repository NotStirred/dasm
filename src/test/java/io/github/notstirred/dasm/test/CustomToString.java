package io.github.notstirred.dasm.test;

import org.assertj.core.presentation.Representation;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

public class CustomToString implements Representation {
    @Override public String toStringOf(Object object) {
        if (object == null) {
            return "null";
        }
        switch (object) {
            case MethodNode methodNode -> {
                return methodNode.desc;
            }
            case MethodNode[] methodNodes -> {
                return String.join(", ", Arrays.stream(methodNodes).map(method -> method.desc).toList());
            }
            case List l -> {
                if (!l.isEmpty() && l.getFirst() instanceof MethodNode) {
                    List<MethodNode> methodNodes = (List<MethodNode>) l;
                    return String.join(", ", methodNodes.stream().map(method -> "Method:{ " + method.name + method.desc + " }").toList());
                }
            }
            case InsnList insnList -> {
                StringBuilder s = new StringBuilder();
                insnList.iterator().forEachRemaining(insnNode -> {
                    s.append(INSN_TYPE_TO_NAME_MAP.get(insnNode.getType())).append(": ").append(insnNode.getOpcode()).append(", ");
                });
                return s.toString();
            }
            default -> { }
        }

        return object.toString();
    }

    @Override public String unambiguousToStringOf(Object object) {
        if (object == null) {
            return "{ null }";
        }
        return Objects.toIdentityString(object);
    }

    private static final Map<Integer, String> INSN_TYPE_TO_NAME_MAP;

    static {
        INSN_TYPE_TO_NAME_MAP = new HashMap<>();
        INSN_TYPE_TO_NAME_MAP.put(0, "INSN");
        INSN_TYPE_TO_NAME_MAP.put(1, "INT_INSN");
        INSN_TYPE_TO_NAME_MAP.put(2, "VAR_INSN");
        INSN_TYPE_TO_NAME_MAP.put(3, "TYPE_INSN");
        INSN_TYPE_TO_NAME_MAP.put(4, "FIELD_INSN");
        INSN_TYPE_TO_NAME_MAP.put(5, "METHOD_INSN");
        INSN_TYPE_TO_NAME_MAP.put(6, "INVOKE_DYNAMIC_INSN");
        INSN_TYPE_TO_NAME_MAP.put(7, "JUMP_INSN");
        INSN_TYPE_TO_NAME_MAP.put(8, "LABEL");
        INSN_TYPE_TO_NAME_MAP.put(9, "LDC_INSN");
        INSN_TYPE_TO_NAME_MAP.put(10, "IINC_INSN");
        INSN_TYPE_TO_NAME_MAP.put(11, "TABLESWITCH_INSN");
        INSN_TYPE_TO_NAME_MAP.put(12, "LOOKUPSWITCH_INSN");
        INSN_TYPE_TO_NAME_MAP.put(13, "MULTIANEWARRAY_INSN");
        INSN_TYPE_TO_NAME_MAP.put(14, "FRAME");
        INSN_TYPE_TO_NAME_MAP.put(15, "LINE");
    }
}
