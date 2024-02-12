package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.DasmTransformException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.TypeUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.getObjectType;

public class Transformer {
    private static final Logger LOGGER = LogManager.getLogger();

    private final ClassNodeProvider classNodeProvider;
    private final MappingsProvider mappingsProvider;

    public Transformer(ClassNodeProvider classNodeProvider, MappingsProvider mappingsProvider) {
        this.classNodeProvider = classNodeProvider;
        this.mappingsProvider = mappingsProvider;
    }

    public void transform(ClassNode targetClass, ClassTransform transform) {

    }

    public void transform(ClassNode targetClass, Collection<MethodTransform> transforms) throws DasmWrappedExceptions {
        DasmClassExceptions dasmClassExceptions = new DasmClassExceptions("An exception occurred when transforming", targetClass);

        Type targetClassType = Type.getType(TypeUtil.classNameToDescriptor(targetClass.name));
        for (MethodTransform transform : transforms) {
            Type methodSrcOwner = transform.srcMethod().owner();

            ClassNode srcClass;
            if (methodSrcOwner.equals(targetClassType)) {
                srcClass = targetClass;
            } else {
                try {
                    srcClass = this.classNodeProvider.classNode(methodSrcOwner);
                } catch (NoSuchTypeExists e) {
                    dasmClassExceptions.addException(e);
                    continue;
                }
            }

            // FIXME: synthetic accessor methods
            // FIXME: add non-clone redirects
            MethodNode methodNode;
            TransformRedirects transformRedirects = new TransformRedirects(transform.redirectSets(), this.mappingsProvider);
            try {
                methodNode = cloneAndApplyRedirects(srcClass, targetClass, transform.srcMethod(), transform.dstMethodName(),
                        transformRedirects, true
                );
            } catch (SrcMethodNotFound e) {
                dasmClassExceptions.addException(e);
                continue;
            }
        }

        dasmClassExceptions.throwIfHasWrapped();
    }

    /**
     * @param redirects lambda redirects are implicitly added, so the parameter is modified.
     */
    private MethodNode cloneAndApplyRedirects(ClassNode srcClass, ClassNode targetClass, ClassMethod classMethod, String dstMethodName,
                                              TransformRedirects redirects, boolean debugLogging) throws SrcMethodNotFound {
        Method existingMethod = classMethod.remap(this.mappingsProvider).method();

        MethodNode originalMethod = srcClass.methods.stream()
                .filter(method -> existingMethod.getName().equals(method.name) && existingMethod.getDescriptor().equals(method.desc))
                .findAny().orElseThrow(() -> new SrcMethodNotFound(classMethod, existingMethod));

        cloneAndApplyLambdaRedirects(srcClass, targetClass, originalMethod, redirects, debugLogging);

        String dstMethodDescriptor = applyTransformsToMethodDescriptor(originalMethod, redirects);

        MethodNode dstMethod = removeExistingMethod(targetClass, dstMethodName, dstMethodDescriptor);
        if (dstMethod != null && (dstMethod.access & ACC_NATIVE) == 0) {
            LOGGER.debug("Method transform overwriting existing method " + dstMethodName + " " + dstMethodDescriptor);
        } else {
            // FIXME: transform exceptions
            dstMethod = new MethodNode(originalMethod.access, dstMethodName, dstMethodDescriptor, null, originalMethod.exceptions.toArray(new String[0]));
        }

        // FIXME: line numbers
        RedirectVisitor redirectVisitor = new RedirectVisitor(new MethodVisitor(ASM9, dstMethod) { }, redirects, this.mappingsProvider);
        originalMethod.accept(redirectVisitor);

        dstMethod.name = dstMethodName;

        targetClass.methods.add(dstMethod);
        return dstMethod;
    }

    private String applyTransformsToMethodDescriptor(MethodNode method, TransformRedirects redirects) {
        Type[] parameterTypes = Type.getArgumentTypes(method.desc);
        Type returnType = Type.getReturnType(method.desc);

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].getSort() == Type.OBJECT) {
                Type type = redirects.typeRedirects().get(parameterTypes[i]);
                parameterTypes[i] = type != null ? type : parameterTypes[i];
            }
        }

        if (returnType.getSort() == Type.OBJECT) {
            returnType = redirects.typeRedirects().getOrDefault(returnType, returnType);
        }

        return Type.getMethodDescriptor(returnType, parameterTypes);
    }

    private void cloneAndApplyLambdaRedirects(ClassNode srcClass, ClassNode targetClass, MethodNode method, TransformRedirects redirects,
                                              boolean debugLogging) throws SrcMethodNotFound {
        Map<Handle, String> lambdaRedirects = new HashMap<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getOpcode() == INVOKEDYNAMIC) {
                InvokeDynamicInsnNode invoke = (InvokeDynamicInsnNode) instruction;
                String bootstrapMethodName = invoke.bsm.getName();
                String bootstrapMethodOwner = invoke.bsm.getOwner();
                if (bootstrapMethodName.equals("metafactory") && bootstrapMethodOwner.equals("java/lang/invoke/LambdaMetafactory")) {
                    for (Object bsmArg : invoke.bsmArgs) {
                        if (!(bsmArg instanceof Handle)) {
                            continue;
                        }
                        Handle handle = (Handle) bsmArg;
                        String owner = handle.getOwner();
                        if (!owner.equals(srcClass.name)) {
                            continue;
                        }
                        String name = handle.getName();
                        String desc = handle.getDesc();
                        // ignore method references into own class
                        MethodNode targetNode =
                                srcClass.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(desc)).findFirst().orElse(null);
                        if (targetNode == null || (targetNode.access & ACC_SYNTHETIC) == 0) {
                            continue;
                        }
                        String newName = "dasm$redirect$" + name;
                        lambdaRedirects.put(handle, newName);
                        cloneAndApplyRedirects(
                                srcClass,
                                targetClass,
                                new ClassMethod(Type.getObjectType(handle.getOwner()), new Method(name, desc)),
                                newName,
                                redirects,
                                debugLogging
                        );
                    }
                }
            }
        }

        Type targetClassType = Type.getType(TypeUtil.classNameToDescriptor(targetClass.name));
        for (Handle handle : lambdaRedirects.keySet()) {
            ClassMethod classMethodLambda = new ClassMethod(getObjectType(handle.getOwner()), new Method(handle.getName(), handle.getDesc()));
            redirects.addLambdaRedirect(
                    classMethodLambda,
                    new MethodRedirectImpl(classMethodLambda, targetClassType, lambdaRedirects.get(handle), (targetClass.access & ACC_INTERFACE) != 0)
            );
        }
    }

    @Nullable
    private static MethodNode removeExistingMethod(ClassNode node, String name, String desc) {
        MethodNode methodNode = node.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(desc)).findAny().orElse(null);
        if (methodNode != null)
            node.methods.remove(methodNode);
        return methodNode;
    }

    @Getter
    public static class SrcMethodNotFound extends DasmTransformException {
        public SrcMethodNotFound(ClassMethod method, Method remappedMethod) {
            super("Could not find source method: `" + method.method().getName() + "` | remapped: `" + remappedMethod.getName() + "`");
        }
    }
}
