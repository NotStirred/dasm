package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.annotation.parse.redirects.ConstructorToFactoryRedirectImpl;
import lombok.Data;
import org.objectweb.asm.*;

import java.util.*;

import static org.objectweb.asm.Opcodes.ASM9;

/**
 * This class <i><b>DOES NOT</b></i> support non-trivial NEW and INVOKESPECIAL calls (where the INVOKESPECIAL is in a branch, for example.)
 */
public class ConstructorToFactoryBufferingVisitor extends MethodVisitor {
    private final Deque<NewInsnData> newStack = new ArrayDeque<>();
    private final Deque<List<Runnable>> instructionsStack = new ArrayDeque<>();

    private final Map<String, ConstructorToFactoryRedirectImpl> redirects;

    protected ConstructorToFactoryBufferingVisitor(MethodVisitor mv, TransformRedirects redirects) {
        super(ASM9, mv);

        this.redirects = new HashMap<>();
        redirects.constructorToFactoryRedirects().forEach((classMethod, constructorToFactoryRedirect) -> {
            this.redirects.put(
                    classMethod.owner().getInternalName() + "." + classMethod.method().getName() + classMethod.method().getDescriptor(),
                    constructorToFactoryRedirect
            );
        });
    }

    private boolean noInstructionsStack() {
        return instructionsStack.peek() == null;
    }

    private List<Runnable> topInsnStack() {
        return instructionsStack.peek();
    }

    @Override public void visitTypeInsn(int opcode, String type) {
        if (opcode == Opcodes.NEW) {
            newStack.push(new NewInsnData(type));
            instructionsStack.push(new ArrayList<>());
        } else if (noInstructionsStack()) {
            super.visitTypeInsn(opcode, type);
        } else {
            topInsnStack().add(() -> super.visitTypeInsn(opcode, type));
        }
    }

    @Override public void visitInsn(int opcode) {
        if (noInstructionsStack()) {
            super.visitInsn(opcode);
        } else {
            if (opcode == Opcodes.DUP && topInsnStack().isEmpty() && !newStack.isEmpty() && !newStack.peek().consumedDup) {
                // Don't add the DUP instruction immediately after a NEW instruction.
                // If necessary it MUST be added again (in visitMethodInsn).
                newStack.peek().consumedDup = true;
                return;
            }
            topInsnStack().add(() -> super.visitInsn(opcode));
        }
    }

    @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (noInstructionsStack()) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        } else if (opcode != Opcodes.INVOKESPECIAL) {
            topInsnStack().add(() -> super.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
            return;
        }

        NewInsnData newInsn = newStack.pop();
        List<Runnable> instructions = instructionsStack.pop();

        ConstructorToFactoryRedirectImpl redirect = this.redirects.get(owner + "." + name + descriptor);
        // If there is no redirect, we keep the instruction, otherwise we don't add it.
        if (redirect == null) {
            super.visitTypeInsn(Opcodes.NEW, newInsn.type);
            if (newInsn.consumedDup()) {
                super.visitInsn(Opcodes.DUP); // DUP insns immediately after NEW insns are unconditionally removed, see visitInsn
            }
        }

        // Add all buffered instructions between the last NEW and this instruction
        instructions.forEach(Runnable::run);
        // Unconditionally add the UNCHANGED invokespecial. Changing it is up to RedirectVisitor (due to invariant 1).
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override public void visitParameter(String name, int access) {
        if (noInstructionsStack()) {
            super.visitParameter(name, access);
        } else {
            topInsnStack().add(() -> super.visitParameter(name, access));
        }
    }

    @Override public void visitAttribute(Attribute attribute) {
        if (noInstructionsStack()) {
            super.visitAttribute(attribute);
        } else {
            topInsnStack().add(() -> super.visitAttribute(attribute));
        }
    }

    @Override public void visitCode() {
        if (noInstructionsStack()) {
            super.visitCode();
        } else {
            topInsnStack().add(super::visitCode);
        }
    }

    @Override public void visitFrame(int type, int numLocal, Object[] local, int numStack, Object[] stack) {
        if (noInstructionsStack()) {
            super.visitFrame(type, numLocal, local, numStack, stack);
        } else {
            topInsnStack().add(() -> super.visitFrame(type, numLocal, local, numStack, stack));
        }
    }

    @Override public void visitIntInsn(int opcode, int operand) {
        if (noInstructionsStack()) {
            super.visitIntInsn(opcode, operand);
        } else {
            topInsnStack().add(() -> super.visitIntInsn(opcode, operand));
        }
    }

    @Override public void visitVarInsn(int opcode, int varIndex) {
        if (noInstructionsStack()) {
            super.visitVarInsn(opcode, varIndex);
        } else {
            topInsnStack().add(() -> super.visitVarInsn(opcode, varIndex));
        }
    }

    @Override public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        if (noInstructionsStack()) {
            super.visitFieldInsn(opcode, owner, name, descriptor);
        } else {
            topInsnStack().add(() -> super.visitFieldInsn(opcode, owner, name, descriptor));
        }
    }

    @Override public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        if (noInstructionsStack()) {
            super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
        } else {
            topInsnStack().add(() -> super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments));
        }
    }

    @Override public void visitJumpInsn(int opcode, Label label) {
        if (noInstructionsStack()) {
            super.visitJumpInsn(opcode, label);
        } else {
            topInsnStack().add(() -> super.visitJumpInsn(opcode, label));
        }
    }

    @Override public void visitLabel(Label label) {
        if (noInstructionsStack()) {
            super.visitLabel(label);
        } else {
            topInsnStack().add(() -> super.visitLabel(label));
        }
    }

    @Override public void visitLdcInsn(Object value) {
        if (noInstructionsStack()) {
            super.visitLdcInsn(value);
        } else {
            topInsnStack().add(() -> super.visitLdcInsn(value));
        }
    }

    @Override public void visitIincInsn(int varIndex, int increment) {
        if (noInstructionsStack()) {
            super.visitIincInsn(varIndex, increment);
        } else {
            topInsnStack().add(() -> super.visitIincInsn(varIndex, increment));
        }
    }

    @Override public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        if (noInstructionsStack()) {
            super.visitTableSwitchInsn(min, max, dflt, labels);
        } else {
            topInsnStack().add(() -> super.visitTableSwitchInsn(min, max, dflt, labels));
        }
    }

    @Override public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        if (noInstructionsStack()) {
            super.visitLookupSwitchInsn(dflt, keys, labels);
        } else {
            topInsnStack().add(() -> super.visitLookupSwitchInsn(dflt, keys, labels));
        }
    }

    @Override public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        if (noInstructionsStack()) {
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        } else {
            topInsnStack().add(() -> super.visitMultiANewArrayInsn(descriptor, numDimensions));
        }
    }

    @Override public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        if (noInstructionsStack()) {
            super.visitTryCatchBlock(start, end, handler, type);
        } else {
            topInsnStack().add(() -> super.visitTryCatchBlock(start, end, handler, type));
        }
    }

    @Override public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        if (noInstructionsStack()) {
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
        } else {
            topInsnStack().add(() -> super.visitLocalVariable(name, descriptor, signature, start, end, index));
        }
    }

    @Override public void visitLineNumber(int line, Label start) {
        if (noInstructionsStack()) {
            super.visitLineNumber(line, start);
        } else {
            topInsnStack().add(() -> super.visitLineNumber(line, start));
        }
    }

    @Override public void visitMaxs(int maxStack, int maxLocals) {
        if (noInstructionsStack()) {
            super.visitMaxs(maxStack, maxLocals);
        } else {
            topInsnStack().add(() -> super.visitMaxs(maxStack, maxLocals));
        }
    }

    @Override public void visitEnd() {
        if (!this.newStack.isEmpty() || !this.instructionsStack.isEmpty()) {
            throw new IllegalStateException("Illegal stack state, had remaining instructions.");
        } else {
            super.visitEnd();
        }
    }

    @Data
    private static class NewInsnData {
        private final String type;
        private boolean consumedDup;
    }
}
