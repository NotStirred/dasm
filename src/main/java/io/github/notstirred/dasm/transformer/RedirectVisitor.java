package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.annotation.parse.redirects.ConstructorToFactoryRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.transformer.exception.*;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;

import java.util.HashMap;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getArgumentTypes;

public class RedirectVisitor extends MethodVisitor {
    private final MappingsProvider mappingsProvider;

    /** Two type redirects maps exist for fast lookup for internal names, and descriptors */
    private final HashMap<String, String> typeRedirects;
    private final HashMap<String, String> typeRedirectsDescriptors;
    private final HashMap<String, FieldRedirectImpl> fieldRedirects;
    private final HashMap<String, MethodRedirectImpl> methodRedirects;
    private final HashMap<String, FieldToMethodRedirectImpl> fieldToMethodRedirects;
    private final HashMap<String, ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects;

    @Override public void visitTypeInsn(int opcode, String typeInternalName) {
        String redirectedInternalName = this.typeRedirects.get(typeInternalName);
        if (redirectedInternalName == null) {
            super.visitTypeInsn(opcode, typeInternalName);
            return;
        }

        super.visitTypeInsn(opcode, redirectedInternalName);
    }

    @Override public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        String redirectedDescriptor = this.typeRedirectsDescriptors.get(descriptor);
        if (redirectedDescriptor == null) {
            super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        super.visitMultiANewArrayInsn(redirectedDescriptor, numDimensions);
    }

    @Override public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        String redirectedType = this.typeRedirects.get(type);
        if (redirectedType == null) {
            super.visitTryCatchBlock(start, end, handler, type);
        }

        super.visitTryCatchBlock(start, end, handler, redirectedType);
    }

    @Override public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        String redirectedDescriptor = this.typeRedirectsDescriptors.get(descriptor);
        if (redirectedDescriptor == null) {
            super.visitLocalVariable(name, descriptor, signature, start, end, index);
            return;
        }

        // TODO: handle generic signatures for redirects? Probably too much effort.
        super.visitLocalVariable(name, redirectedDescriptor, null, start, end, index);
    }

    public RedirectVisitor(MethodVisitor mv, TransformRedirects redirects, MappingsProvider mappingsProvider) {
        super(ASM9, mv);
        this.mappingsProvider = mappingsProvider;

        this.typeRedirects = new HashMap<>();
        this.typeRedirectsDescriptors = new HashMap<>();
        redirects.typeRedirects().forEach((srcType, dstType) -> {
            this.typeRedirects.put(srcType.getInternalName(), dstType.getInternalName());
            this.typeRedirectsDescriptors.put(srcType.getDescriptor(), dstType.getDescriptor());
        });

        this.methodRedirects = new HashMap<>();
        redirects.methodRedirects().forEach((classMethodUnmapped, methodRedirect) -> {
            ClassMethod classMethod = classMethodUnmapped.remap(this.mappingsProvider);
            this.methodRedirects.put(
                    classMethod.owner().getInternalName() + "." + classMethod.method().getName() + classMethod.method().getDescriptor(),
                    methodRedirect
            );
        });

        this.fieldRedirects = new HashMap<>();
        redirects.fieldRedirects().forEach((classFieldUnmapped, fieldRedirect) -> {
            ClassField classField = classFieldUnmapped.remap(this.mappingsProvider);
            this.fieldRedirects.put(
                    classField.owner().getInternalName() + "." + classField.name(),
                    fieldRedirect
            );
        });

        this.fieldToMethodRedirects = new HashMap<>();
        redirects.fieldToMethodRedirects().forEach((classFieldUnmapped, fieldToMethodRedirect) -> {
            ClassField classField = classFieldUnmapped.remap(this.mappingsProvider);
            this.fieldToMethodRedirects.put(
                    classField.owner().getInternalName() + "." + classField.name(),
                    fieldToMethodRedirect
            );
        });

        this.constructorToFactoryRedirects = new HashMap<>();
        redirects.constructorToFactoryRedirects().forEach((classMethodUnmapped, constructorToFactoryRedirect) -> {
            ClassMethod classMethod = classMethodUnmapped.remap(this.mappingsProvider);
            this.constructorToFactoryRedirects.put(
                    classMethod.owner().getInternalName() + "." + classMethod.method().getName() + classMethod.method().getDescriptor(),
                    constructorToFactoryRedirect
            );
        });
    }


    @SneakyThrows({ FieldToMethodPutFieldWithoutSetterMethod.class, FieldRedirectingToFieldAndMethod.class,
            RedirectChangesOwnerWithIncompatibleTypeRedirect.class, RedirectChangesOwnerWithoutTypeRedirect.class,
            FieldToMethodRedirectInvalidStaticity.class })
    @Override public void visitFieldInsn(int opcode, String currentOwner, String name, String descriptor) {
        String key = currentOwner + "." + name;
        FieldRedirectImpl redirectedField = fieldRedirects.get(key);
        FieldToMethodRedirectImpl fieldToMethodRedirect = fieldToMethodRedirects.get(key);
        if (redirectedField == null && fieldToMethodRedirect == null) {
            super.visitFieldInsn(opcode, currentOwner, name, descriptor);
            return;
        } else if (redirectedField != null && fieldToMethodRedirect != null) {
            throw new FieldRedirectingToFieldAndMethod(redirectedField, fieldToMethodRedirect);
        }

        if (fieldToMethodRedirect != null) {
            doFieldToMethodRedirect(opcode, currentOwner, name, descriptor, fieldToMethodRedirect);
            return;
        }

        doFieldRedirect(opcode, currentOwner, descriptor, redirectedField);
    }

    @SneakyThrows({ MethodRedirectingToMethodAndFactory.class, RedirectChangesOwnerWithoutTypeRedirect.class,
            RedirectChangesOwnerWithIncompatibleTypeRedirect.class })
    @Override public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        String key = owner + "." + name + descriptor;
        MethodRedirectImpl redirectedMethod = methodRedirects.get(key);
        ConstructorToFactoryRedirectImpl constructorToFactoryRedirect = constructorToFactoryRedirects.get(key);
        if (redirectedMethod == null && constructorToFactoryRedirect == null) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        } else if (redirectedMethod != null && constructorToFactoryRedirect != null) {
            throw new MethodRedirectingToMethodAndFactory();
        }

        if (constructorToFactoryRedirect != null) {
            throw new Error("Constructor to factory redirect not implemented!");
        }

        doMethodRedirect(opcode, owner, descriptor, redirectedMethod);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bsm, Object... bsmArgs) {
        // handles method references
        String bootstrapMethodName = bsm.getName();
        String bootstrapMethodOwner = bsm.getOwner();
        if (bootstrapMethodName.equals("metafactory") && bootstrapMethodOwner.equals("java/lang/invoke/LambdaMetafactory")) {
            for (int i = 0; i < bsmArgs.length; i++) {
                Object bsmArg = bsmArgs[i];
                if (bsmArg instanceof Handle) {
                    Handle handle = (Handle) bsmArg;
                    String lambdaOrReferenceMethodOwner = handle.getOwner();
                    String lambdaOrReferenceMethodName = handle.getName();
                    String lambdaOrReferenceMethodDesc = handle.getDesc();

                    String key = lambdaOrReferenceMethodOwner + "." + lambdaOrReferenceMethodName + lambdaOrReferenceMethodDesc;
                    MethodRedirectImpl redirectedMethod = methodRedirects.get(key);
                    if (redirectedMethod == null) {
                        break; // done, no redirect
                    }
                    // FIXME:  shouldn't use INVOKESTATIC, also should check owner matches between methodredirect and typeredirects, (same as field/methodredirects)
                    int tag = handle.getTag();
                    if (tag == Opcodes.H_INVOKESPECIAL || tag == Opcodes.H_NEWINVOKESPECIAL) {
                        throw new RuntimeException("Can't redirect INVOKESPECIAL to different class.");
                    }
                    if (tag == Opcodes.H_INVOKEVIRTUAL || tag == Opcodes.H_INVOKEINTERFACE) {
                        tag = Opcodes.H_INVOKESTATIC;
                        lambdaOrReferenceMethodDesc = addOwnerAsFirstArgument(lambdaOrReferenceMethodOwner, lambdaOrReferenceMethodDesc);
                    } else if (tag != Opcodes.H_INVOKESTATIC) {
                        throw new RuntimeException("Method redirect to different class: Only INVOKEVIRTUAL, INVOKEINTERFACE and INVOKESTATIC supported");
                    }
                    Handle newHandle = new Handle(tag, redirectedMethod.dstOwner().getInternalName(), redirectedMethod.dstName(),
                            lambdaOrReferenceMethodDesc, redirectedMethod.isDstOwnerInterface()
                    );
                    Object[] newBsmArgs = bsmArgs.clone();
                    newBsmArgs[i] = newHandle;
                    super.visitInvokeDynamicInsn(name, descriptor, bsm, newBsmArgs);
                    return; // done, redirected
                }
            }
        }
        super.visitInvokeDynamicInsn(name, descriptor, bsm, bsmArgs);
    }

    @NotNull private String addOwnerAsFirstArgument(String owner, String descriptor) {
        Type[] argumentTypes = getArgumentTypes(descriptor);
        Type retType = Type.getReturnType(descriptor);
        Type[] newArgs = new Type[argumentTypes.length + 1];
        newArgs[0] = Type.getObjectType(owner);
        System.arraycopy(argumentTypes, 0, newArgs, 1, argumentTypes.length);
        descriptor = Type.getMethodDescriptor(retType, newArgs);
        return descriptor;
    }

    private void doFieldToMethodRedirect(int opcode, String currentOwner, String name, String descriptor, FieldToMethodRedirectImpl fieldToMethodRedirect)
            throws FieldToMethodPutFieldWithoutSetterMethod, FieldToMethodRedirectInvalidStaticity,
            RedirectChangesOwnerWithoutTypeRedirect, RedirectChangesOwnerWithIncompatibleTypeRedirect {
        boolean opcodeIsStatic = opcode == GETSTATIC | opcode == PUTSTATIC;

        if (opcodeIsStatic != fieldToMethodRedirect.isStatic()) {
            throw new FieldToMethodRedirectInvalidStaticity(fieldToMethodRedirect);
        }

        ClassMethod method = (opcode == GETFIELD || opcode == GETSTATIC) ?
                fieldToMethodRedirect.getterDstMethod() :
                fieldToMethodRedirect.setterDstMethod()
                        .orElseThrow(() -> new FieldToMethodPutFieldWithoutSetterMethod(currentOwner, descriptor, name));

        if (fieldToMethodRedirect.isStatic()) {
            // Static field to method redirects are allowed to change owner
            super.visitMethodInsn(
                    fieldToMethodRedirect.isDstOwnerInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                    method.owner().getInternalName(),
                    method.method().getName(),
                    method.method().getDescriptor(),
                    fieldToMethodRedirect.isDstOwnerInterface()
            );
            return;
        }

        String typeRedirectOwner = this.typeRedirects.get(currentOwner);
        String fieldToMethodRedirectNewOwner = method.owner().getInternalName();

        // Non-static field to method redirects must have a corresponding type redirect if they change owner
        if (!fieldToMethodRedirectNewOwner.equals(currentOwner) && typeRedirectOwner == null) {
            throw new RedirectChangesOwnerWithoutTypeRedirect();
        }
        if (!fieldToMethodRedirectNewOwner.equals(typeRedirectOwner)) {
            throw new RedirectChangesOwnerWithIncompatibleTypeRedirect();
        }

        super.visitMethodInsn(
                fieldToMethodRedirect.isDstOwnerInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                fieldToMethodRedirectNewOwner,
                method.method().getName(),
                method.method().getDescriptor(),
                fieldToMethodRedirect.isDstOwnerInterface()
        );
    }


    private void doFieldRedirect(int opcode, String currentOwner, String descriptor, FieldRedirectImpl redirectedField)
            throws RedirectChangesOwnerWithoutTypeRedirect, RedirectChangesOwnerWithIncompatibleTypeRedirect {
        // Static field redirects may change owner without a type redirect
        if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
            super.visitFieldInsn(opcode, redirectedField.dstOwner().getInternalName(), redirectedField.dstName(), descriptor);
            return;
        }

        String typeRedirectOwner = this.typeRedirects.get(currentOwner);
        String fieldRedirectNewOwner = redirectedField.dstOwner().getInternalName();

        // Non-static field redirects must have a corresponding type redirect if they change owner
        if (!fieldRedirectNewOwner.equals(currentOwner) && typeRedirectOwner == null) {
            throw new RedirectChangesOwnerWithoutTypeRedirect();
        }
        if (!fieldRedirectNewOwner.equals(typeRedirectOwner)) {
            throw new RedirectChangesOwnerWithIncompatibleTypeRedirect();
        }

        super.visitFieldInsn(opcode, fieldRedirectNewOwner, redirectedField.dstName(), descriptor);
    }

    private void doMethodRedirect(int opcode, String currentOwner, String descriptor, MethodRedirectImpl methodRedirect)
            throws RedirectChangesOwnerWithoutTypeRedirect, RedirectChangesOwnerWithIncompatibleTypeRedirect {
        if (opcode == Opcodes.INVOKESPECIAL) {
            // FIXME: don't use unchecked exceptions
            throw new RuntimeException("Can't redirect INVOKESPECIAL to different class.");
        }
        if (!(opcode == INVOKEVIRTUAL || opcode == INVOKEINTERFACE || opcode == Opcodes.INVOKESTATIC)) {
            // FIXME: don't use unchecked exceptions
            throw new RuntimeException("Method redirect: Only INVOKEVIRTUAL, INVOKEINTERFACE and INVOKESTATIC supported");
        }

        String typeRedirectOwner = this.typeRedirects.get(currentOwner);
        String methodRedirectNewOwner = methodRedirect.dstOwner().getInternalName();

        // Non-static field redirects must have a corresponding type redirect if they change owner
        if (typeRedirectOwner == null) {
            // If there is no type redirect, the method redirect must not change owner
            if (!methodRedirectNewOwner.equals(currentOwner)) {
                throw new RedirectChangesOwnerWithoutTypeRedirect();
            }
        } else {
            // If there is a type redirect, the method redirect must change owner to the same owner as the type redirect
            if (!methodRedirectNewOwner.equals(typeRedirectOwner)) {
                throw new RedirectChangesOwnerWithIncompatibleTypeRedirect();
            }
        }

        super.visitMethodInsn(
                opcode,
                methodRedirect.dstOwner().getInternalName(),
                methodRedirect.dstName(),
                descriptor,
                methodRedirect.isDstOwnerInterface()
        );
    }
}
