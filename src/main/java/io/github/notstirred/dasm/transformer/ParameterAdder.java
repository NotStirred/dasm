package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.annotation.parse.AddedParameter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;

import static org.objectweb.asm.Opcodes.ASM9;

public class ParameterAdder extends MethodVisitor {
    private final List<AddedParameter> addedParameters;

    private int localVariables;

    private int maxStack;
    private int maxLocals;

    private Label startLabel;
    private Label endLabel;

    public ParameterAdder(MethodVisitor visitor, List<AddedParameter> addedParameters) {
        super(ASM9, visitor);

        this.addedParameters = addedParameters;
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);

        if (this.startLabel == null) {
            this.startLabel = label;
        }
        this.endLabel = label;
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, descriptor, signature, start, end, index);
        this.localVariables++;
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        this.maxStack = maxStack;
        this.maxLocals = maxLocals;
    }

    @Override
    public void visitEnd() {
        for (AddedParameter addedParameter : this.addedParameters) {
            super.visitLocalVariable("foo", addedParameter.type().getDescriptor(), null, this.startLabel, this.endLabel, this.localVariables);
        }
        super.visitMaxs(this.maxStack, this.maxLocals + this.addedParameters.size());
        super.visitEnd();
    }
}
