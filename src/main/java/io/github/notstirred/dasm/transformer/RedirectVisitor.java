package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.annotation.parse.redirects.ConstructorToFactoryRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.transformer.data.BuiltRedirects;
import io.github.notstirred.dasm.transformer.data.TransformRedirects;
import io.github.notstirred.dasm.transformer.exception.*;
import lombok.SneakyThrows;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import static io.github.notstirred.dasm.transformer.TypeRemapper.SKIP_TYPE_REDIRECT_PREFIX;
import static org.objectweb.asm.Opcodes.*;

public class RedirectVisitor extends MethodVisitor {
    private final BuiltRedirects redirects;

    public RedirectVisitor(MethodVisitor mv, TransformRedirects redirects, MappingsProvider mappingsProvider) {
        super(ASM9, mv);
        this.redirects = new BuiltRedirects(redirects, mappingsProvider);
    }

    @SneakyThrows({FieldToMethodPutFieldWithoutSetterMethod.class, FieldRedirectingToFieldAndMethod.class,
            RedirectChangesOwnerWithIncompatibleTypeRedirect.class, RedirectChangesOwnerWithoutTypeRedirect.class,
            FieldToMethodRedirectInvalidStaticity.class})
    @Override
    public void visitFieldInsn(int opcode, String currentOwner, String name, String descriptor) {
        String key = currentOwner + "." + name;
        FieldRedirectImpl fieldRedirect = this.redirects.fieldRedirects().get(key);
        FieldToMethodRedirectImpl fieldToMethodRedirect = this.redirects.fieldToMethodRedirects().get(key);
        if (fieldRedirect == null && fieldToMethodRedirect == null) {
            super.visitFieldInsn(opcode, currentOwner, name, descriptor);
            return;
        } else if (fieldRedirect != null && fieldToMethodRedirect != null) {
            throw new FieldRedirectingToFieldAndMethod(fieldRedirect, fieldToMethodRedirect);
        }

        if (fieldToMethodRedirect != null) {
            doFieldToMethodRedirect(opcode, currentOwner, name, descriptor, fieldToMethodRedirect);
            return;
        }

        doFieldRedirect(opcode, currentOwner, descriptor, fieldRedirect);
    }

    @SneakyThrows({MethodRedirectingToMethodAndFactory.class, RedirectChangesOwnerWithoutTypeRedirect.class,
            RedirectChangesOwnerWithIncompatibleTypeRedirect.class})
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        String key = owner + "." + name + descriptor;
        MethodRedirectImpl methodRedirect = this.redirects.methodRedirects().get(key);
        ConstructorToFactoryRedirectImpl constructorToFactoryRedirect = this.redirects.constructorToFactoryRedirects().get(key);
        if (methodRedirect == null && constructorToFactoryRedirect == null) {
            // no redirect
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            return;
        } else if (methodRedirect != null && constructorToFactoryRedirect != null) {
            // both redirects exist?! throw
            throw new MethodRedirectingToMethodAndFactory(methodRedirect, constructorToFactoryRedirect);
        }

        if (constructorToFactoryRedirect != null) {
            // if there is a constructorToFactoryRedirect we assume that the NEW instruction was removed by ConstructorToFactoryBufferingVisitor
            // all we need to do is add a different method insn
            super.visitMethodInsn(
                    INVOKESTATIC,
                    SKIP_TYPE_REDIRECT_PREFIX + constructorToFactoryRedirect.dstOwner().getInternalName(),
                    constructorToFactoryRedirect.dstName(),
                    // The return value of the factory must be the src owner of the constructor it's replacing.
                    Type.getMethodDescriptor(constructorToFactoryRedirect.srcConstructor().owner(), Type.getArgumentTypes(descriptor)),
                    constructorToFactoryRedirect.isDstOwnerInterface()
            );
        } else {
            doMethodRedirect(opcode, owner, descriptor, methodRedirect);
        }
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
                    // This is the actual lambda method that gets called, eg: lambda$method$0
                    Handle handle = (Handle) bsmArg;
                    String lambdaOrReferenceMethodOwner = handle.getOwner();
                    String lambdaOrReferenceMethodName = handle.getName();
                    String lambdaOrReferenceMethodDesc = handle.getDesc();

                    String key = lambdaOrReferenceMethodOwner + "." + lambdaOrReferenceMethodName + lambdaOrReferenceMethodDesc;
                    MethodRedirectImpl redirectedMethod = this.redirects.methodRedirects().get(key);

                    // This is the type that the lambda implicitly implements
                    Type functionalInterfaceType = new Method(name, descriptor).getReturnType();
                    String functionalInterfaceKey = functionalInterfaceType.getInternalName() + '.' + name + lambdaOrReferenceMethodDesc;
                    // If the implicitly implemented interface is redirected we should also look for a method redirect for the implicit name of the lambda
                    MethodRedirectImpl functionalInterfaceMethodRedirect = this.redirects.methodRedirects().get(functionalInterfaceKey);

                    if (redirectedMethod == null && functionalInterfaceMethodRedirect == null) {
                        break; // done, no redirect
                    }

                    Object[] newBsmArgs = bsmArgs.clone();
                    if (redirectedMethod != null) {
                        Handle newHandle = new Handle(handle.getTag(), SKIP_TYPE_REDIRECT_PREFIX + redirectedMethod.dstOwner().getInternalName(),
                                redirectedMethod.dstName(),
                                lambdaOrReferenceMethodDesc, redirectedMethod.isDstOwnerInterface()
                        );
                        newBsmArgs[i] = newHandle;
                    }

                    String redirectedFunctionalInterfaceMethodName = functionalInterfaceMethodRedirect == null ? name : functionalInterfaceMethodRedirect.dstName();

                    super.visitInvokeDynamicInsn(redirectedFunctionalInterfaceMethodName, descriptor, bsm, newBsmArgs);
                    return; // done, redirected
                }
            }
        }
        super.visitInvokeDynamicInsn(name, descriptor, bsm, bsmArgs);
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
                        .orElseThrow(() -> new FieldToMethodPutFieldWithoutSetterMethod(currentOwner, name));

        if (fieldToMethodRedirect.isStatic()) {
            // Static field to method redirects are allowed to change owner
            super.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    SKIP_TYPE_REDIRECT_PREFIX + method.owner().getInternalName(),
                    method.method().getName(),
                    method.method().getDescriptor(),
                    fieldToMethodRedirect.isDstOwnerInterface()
            );
            return;
        }

        String typeRedirectOwner = this.redirects.typeRedirects().get(currentOwner);
        String fieldToMethodRedirectNewOwner = method.owner().getInternalName();

        // Non-static field to method redirects must have a corresponding type redirect if they change owner
        if (!fieldToMethodRedirectNewOwner.equals(currentOwner) && typeRedirectOwner == null) {
            throw new RedirectChangesOwnerWithoutTypeRedirect(fieldToMethodRedirect);
        }
        if (typeRedirectOwner != null && !fieldToMethodRedirectNewOwner.equals(typeRedirectOwner)) {
            throw new RedirectChangesOwnerWithIncompatibleTypeRedirect(fieldToMethodRedirect, currentOwner, typeRedirectOwner);
        }

        super.visitMethodInsn(
                fieldToMethodRedirect.isDstOwnerInterface() ? Opcodes.INVOKEINTERFACE : Opcodes.INVOKEVIRTUAL,
                SKIP_TYPE_REDIRECT_PREFIX + fieldToMethodRedirectNewOwner,
                method.method().getName(),
                method.method().getDescriptor(),
                fieldToMethodRedirect.isDstOwnerInterface()
        );
    }


    private void doFieldRedirect(int opcode, String currentOwner, String descriptor, FieldRedirectImpl fieldRedirect)
            throws RedirectChangesOwnerWithoutTypeRedirect, RedirectChangesOwnerWithIncompatibleTypeRedirect {
        // Static field redirects may change owner without a type redirect
        if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
            super.visitFieldInsn(
                    opcode,
                    SKIP_TYPE_REDIRECT_PREFIX + fieldRedirect.dstOwner().getInternalName(),
                    fieldRedirect.dstName(),
                    descriptor
            );
            return;
        }

        String typeRedirectOwner = this.redirects.typeRedirects().get(currentOwner);
        String fieldRedirectNewOwner = fieldRedirect.dstOwner().getInternalName();

        // Non-static field redirects must have a corresponding type redirect if they change owner
        if (!fieldRedirectNewOwner.equals(currentOwner) && typeRedirectOwner == null) {
            throw new RedirectChangesOwnerWithoutTypeRedirect(fieldRedirect);
        }
        if (typeRedirectOwner != null && !fieldRedirectNewOwner.equals(typeRedirectOwner)) {
            throw new RedirectChangesOwnerWithIncompatibleTypeRedirect(fieldRedirect, currentOwner, typeRedirectOwner);
        }

        super.visitFieldInsn(
                opcode,
                SKIP_TYPE_REDIRECT_PREFIX + fieldRedirectNewOwner,
                fieldRedirect.dstName(),
                descriptor
        );
    }

    private void doMethodRedirect(int opcode, String currentOwnerInternal, String descriptor, MethodRedirectImpl methodRedirect)
            throws RedirectChangesOwnerWithoutTypeRedirect, RedirectChangesOwnerWithIncompatibleTypeRedirect {
        if (opcode == Opcodes.INVOKESPECIAL) {
            if (!currentOwnerInternal.equals(methodRedirect.dstOwner().getInternalName())) {
                // FIXME: don't use unchecked exceptions
                throw new RuntimeException("Can't redirect INVOKESPECIAL to different class.");
            }
        }

        String typeRedirectOwner = this.redirects.typeRedirects().get(currentOwnerInternal);
        String methodRedirectNewOwner = methodRedirect.dstOwner().getInternalName();

        // Non-static field redirects must have a corresponding type redirect if they change owner
        if (!methodRedirect.isStatic()) {
            if (typeRedirectOwner == null) {
                // If there is no type redirect, the method redirect must not change owner
                if (!methodRedirectNewOwner.equals(currentOwnerInternal)) {
                    throw new RedirectChangesOwnerWithoutTypeRedirect(methodRedirect);
                }
            } else {
                // If there is a type redirect, the method redirect must change owner to the same owner as the type redirect
                if (!methodRedirectNewOwner.equals(typeRedirectOwner)) {
                    throw new RedirectChangesOwnerWithIncompatibleTypeRedirect(methodRedirect, currentOwnerInternal, methodRedirectNewOwner);
                }
            }
        }

        super.visitMethodInsn(
                opcode == INVOKESPECIAL ? INVOKESPECIAL : methodRedirect.isStatic() ? INVOKESTATIC : methodRedirect.isDstOwnerInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                SKIP_TYPE_REDIRECT_PREFIX + methodRedirect.dstOwner().getInternalName(),
                methodRedirect.dstName(),
                descriptor,
                methodRedirect.isDstOwnerInterface()
        );
    }
}
