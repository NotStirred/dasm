package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.annotation.parse.AddedParameter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.List;

import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ASM9;

/**
 * Adds parameters to method and handles properly offsetting access to any local variables.
 * <p>
 * FIXME: retain parameter names
 */
public class ParameterAdder extends MethodVisitor {
    private final Type owner;
    private final String newMethodDescriptor;
    private final boolean isMethodStatic;

    private final int addedParameterTypeSize; // longs and doubles take up two spaces in the LVT for no reason whatsoever.
    private final int lvtIdxOfFirstLocal;
    private final int[] parameterIndicesLUT; // New lvt index of parameters. Access by lvt index NOT by parameter index.

    private int maxStack;
    private int maxLocals;

    private Label startLabel;
    private Label endLabel;

    public ParameterAdder(MethodNode node, Type owner, String originalMethodDescriptor, String newMethodDescriptor, List<AddedParameter> addedParameters) {
        super(ASM9, node);
        this.owner = owner;
        this.newMethodDescriptor = newMethodDescriptor;
        this.isMethodStatic = (node.access & ACC_STATIC) != 0;

        int lvtSize = Arrays.stream(Type.getArgumentTypes(originalMethodDescriptor)).mapToInt(Type::getSize).sum() + (this.isMethodStatic ? 0 : 1);
        this.lvtIdxOfFirstLocal = lvtSize;
        this.parameterIndicesLUT = new int[lvtSize];

        for (int i = 0; i < parameterIndicesLUT.length; i++) {
            parameterIndicesLUT[i] = i;
        }
        int addedParamSize = 0;
        for (AddedParameter addedParameter : addedParameters) {
            addedParamSize += addedParameter.type().getSize();
            for (int lvtIdx = addedParameter.index() + (this.isMethodStatic ? 0 : 1); lvtIdx < parameterIndicesLUT.length; lvtIdx += addedParameter.type().getSize()) {
                parameterIndicesLUT[lvtIdx]++;
            }
        }
        this.addedParameterTypeSize = addedParamSize;
    }

    @Override
    public void visitCode() {
        super.visitCode();

        // Visit code is called first, so we add all parameter locals here; relevant locals are skipped in visitVarInsn.

        Type[] methodArgs = Type.getArgumentTypes(this.newMethodDescriptor);

        startLabel = new Label();
        endLabel = new Label();
        super.visitLabel(startLabel);

        int localIdx = 0;
        if (!this.isMethodStatic) {
            super.visitLocalVariable("this", this.owner.getDescriptor(), null, startLabel, endLabel, localIdx++);
        }
        for (Type methodArg : methodArgs) {
            super.visitLocalVariable("param" + localIdx, methodArg.getDescriptor(), null, startLabel, endLabel, localIdx);
            localIdx += methodArg.getSize();
        }
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        if (varIndex >= lvtIdxOfFirstLocal) { // a local variable
            super.visitVarInsn(opcode, varIndex + this.addedParameterTypeSize);
        } else { // a parameter
            super.visitVarInsn(opcode, this.parameterIndicesLUT[varIndex]);
        }
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        if (varIndex >= lvtIdxOfFirstLocal) { // a local variable
            super.visitIincInsn(varIndex + this.addedParameterTypeSize, increment);
        } else { // a parameter
            super.visitIincInsn(this.parameterIndicesLUT[varIndex], increment);
        }
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        // We only add local variables, not parameters as they should already be there, see visitCode
        if (index >= this.lvtIdxOfFirstLocal) {
            super.visitLocalVariable(name, descriptor, signature, start, end, index + this.addedParameterTypeSize);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }

    @Override
    public void visitEnd() {
        // endLabel is only null if there is no code (an abstract method), in which case nothing else has been called
        if (this.endLabel != null) {
            super.visitMaxs(this.maxStack, this.maxLocals + this.addedParameterTypeSize);

            super.visitLabel(this.endLabel);
        }
        super.visitEnd();
    }
}
