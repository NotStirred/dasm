package io.github.notstirred.dasm.transformer;

import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;

/**
 * Rewrites method calls for type redirects to make sure they use the correct INVOKE instruction.
 */
public class Interfacicitifier extends MethodVisitor {
    private final Map<String, Boolean> redirectedTypeIsInterface = new HashMap<>();

    protected Interfacicitifier(MethodVisitor methodVisitor, TransformRedirects redirects) {
        super(ASM9, methodVisitor);

        redirects.typeRedirects().forEach((type, typeAndIsInterface) -> {
            redirectedTypeIsInterface.put(typeAndIsInterface.type().getInternalName(), typeAndIsInterface.isInterface());
        });
    }

    @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        boolean redirectedIsInterface = this.redirectedTypeIsInterface.getOrDefault(owner, isInterface);
        int redirectedOpcode;
        switch (opcode) {
            case INVOKEVIRTUAL:
            case INVOKEINTERFACE: {
                redirectedOpcode = redirectedIsInterface ? INVOKEINTERFACE : INVOKEVIRTUAL;
                break;
            }
            case INVOKESPECIAL:
            case INVOKESTATIC: {
                redirectedOpcode = opcode;
                break;
            }
            default:
                throw new IllegalArgumentException("Got invalid opcode for method instruction " + opcode);
        }

        super.visitMethodInsn(redirectedOpcode, owner, name, descriptor, redirectedIsInterface);
    }
}
