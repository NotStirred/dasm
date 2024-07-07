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

public class ParameterAdder extends MethodVisitor {
    private final int addedParameterTypeSize; // longs and doubles take up two spaces in the LVT for no reason whatsoever.
    private final boolean isStatic;
    private final int idxOfFirstLocal;
    private final int[] parameterIndicesLUT;

    private final String newMethodDescriptor;
    private final Type owner;

    private int maxStack;
    private int maxLocals;

    private Label startLabel;
    private Label endLabel;

    public ParameterAdder(MethodNode node, Type owner, String originalMethodDescriptor, String newMethodDescriptor, List<AddedParameter> addedParameters) {
        super(ASM9, node);
        this.owner = owner;
        this.isStatic = (node.access & ACC_STATIC) != 0;
        int lvtSize = Arrays.stream(Type.getArgumentTypes(originalMethodDescriptor)).mapToInt(Type::getSize).sum() + (this.isStatic ? 0 : 1);
        this.idxOfFirstLocal = lvtSize;
        this.newMethodDescriptor = newMethodDescriptor;
        this.parameterIndicesLUT = new int[lvtSize];

        for (int i = 0; i < parameterIndicesLUT.length; i++) {
            parameterIndicesLUT[i] = i;
        }
        int addedParamSize = 0;
        for (AddedParameter addedParameter : addedParameters) {
            addedParamSize += addedParameter.type().getSize();
            for (int i = addedParameter.index() + (this.isStatic ? 0 : 1); i < parameterIndicesLUT.length; i += addedParameter.type().getSize()) {
                parameterIndicesLUT[i]++;
            }
        }
        this.addedParameterTypeSize = addedParamSize;
    }

    @Override
    public void visitCode() {
        super.visitCode();

        Type[] methodArgs = Type.getArgumentTypes(this.newMethodDescriptor);

        endLabel = new Label();
        startLabel = new Label();
        super.visitLabel(startLabel);

        int localIdx = 0;
        if (!this.isStatic) {
            super.visitLocalVariable("this", this.owner.getDescriptor(), null, startLabel, endLabel, localIdx++);
        }
        for (Type methodArg : methodArgs) {
            super.visitLocalVariable("param" + localIdx, methodArg.getDescriptor(), null, startLabel, endLabel, localIdx);
            localIdx += methodArg.getSize();
        }
    }

    @Override
    public void visitVarInsn(int opcode, int varIndex) {
        if (varIndex >= idxOfFirstLocal) { // a local variable
            super.visitVarInsn(opcode, varIndex + this.addedParameterTypeSize);
        } else { // a parameter
            super.visitVarInsn(opcode, this.parameterIndicesLUT[varIndex]);
        }
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        if (varIndex >= idxOfFirstLocal) { // a local variable
            super.visitIincInsn(varIndex + this.addedParameterTypeSize, increment);
        } else { // a parameter
            super.visitIincInsn(this.parameterIndicesLUT[varIndex], increment);
        }
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        if (index >= this.idxOfFirstLocal) {
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
        super.visitMaxs(this.maxStack, this.maxLocals + this.addedParameterTypeSize);

        super.visitLabel(this.endLabel);

        super.visitEnd();
    }
}
