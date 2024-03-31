package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.DasmTransformException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.transformer.data.*;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.TypeUtil;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.MethodRemapper;
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

    public void transform(ClassNode targetClass, ClassTransform transform) throws NoSuchTypeExists {
        ClassNode sourceClass = classNodeProvider.classNode(transform.srcType());

        LOGGER.info("Transforming (" + sourceClass.name + "->" + targetClass.name + "): Transforming whole class");

        ClassNode oldNode = new ClassNode(ASM9);
        targetClass.accept(oldNode);

        oldNode.methods.clear();
        oldNode.fields.clear();

        targetClass.access = 0;
        targetClass.name = null;
        targetClass.signature = null;
        targetClass.superName = null;
        targetClass.interfaces.clear();
        targetClass.sourceFile = null;
        targetClass.sourceDebug = null;
        targetClass.module = null;
        targetClass.outerClass = null;
        targetClass.outerMethod = null;
        targetClass.outerMethodDesc = null;
        targetClass.visibleAnnotations = null;
        targetClass.invisibleAnnotations = null;
        targetClass.visibleTypeAnnotations = null;
        targetClass.invisibleTypeAnnotations = null;
        targetClass.attrs = null;
        targetClass.innerClasses.clear();
        targetClass.nestHostClass = null;
        targetClass.nestMembers = null;
        targetClass.permittedSubclasses = null;
        targetClass.recordComponents = null;
        targetClass.fields.clear();
        targetClass.methods.clear();


        TransformRedirects redirects = new TransformRedirects(transform.redirectSets(), this.mappingsProvider);
        BuiltRedirects builtRedirects = new BuiltRedirects(redirects, this.mappingsProvider);

        // TODO: could type redirects be better accomplished by a remapper that skips method bodies (bc redirect chaining)?
        ClassVisitor cv = new ClassVisitor(ASM9, targetClass) {
            @Override public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                String redirectedDescriptor = builtRedirects.typeRedirectsDescriptors().getOrDefault(descriptor, descriptor);
                String key = sourceClass.name + "." + name;

                FieldRedirectImpl fieldRedirect = builtRedirects.fieldRedirects().get(key);
                if (fieldRedirect != null) {
                    return super.visitField(access, fieldRedirect.dstName(), redirectedDescriptor, null, value);
                } else {
                    return super.visitField(access, name, descriptor, redirectedDescriptor, value);
                }
            }

            @Override public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                String key = sourceClass.name + "." + name + descriptor;
                // here we manually get the redirect, as the remappers only look within a method, not at the definition
                MethodRedirectImpl methodRedirect = builtRedirects.methodRedirects().get(key);
                String dstName = methodRedirect == null ? name : methodRedirect.dstName();

                String redirectedDescriptor = applyTransformsToMethodDescriptor(descriptor, redirects);

                return dasmTransformingVisitor(
                        super.visitMethod(access, dstName, redirectedDescriptor, signature, exceptions),
                        redirects,
                        mappingsProvider
                );
            }
        };
        sourceClass.accept(cv);
        oldNode.accept(cv);
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
            TransformRedirects transformRedirects = new TransformRedirects(transform.redirectSets(), this.mappingsProvider);
            try {
                if (transform.inPlace()) {
                    applyRedirects(srcClass, transform.srcMethod(), transformRedirects, true);
                } else {
                    cloneAndApplyRedirects(srcClass, targetClass, transform.srcMethod(), transform.dstMethodName(), transformRedirects, true);
                }

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
    private MethodNode cloneAndApplyRedirects(ClassNode srcClass, ClassNode targetClass, ClassMethod srcMethod, String dstMethodName,
                                              TransformRedirects redirects, boolean debugLogging) throws SrcMethodNotFound {
        Method existingMethod = srcMethod.remap(this.mappingsProvider).method();

        MethodNode srcMethodNode = srcClass.methods.stream()
                .filter(method -> existingMethod.getName().equals(method.name) && existingMethod.getDescriptor().equals(method.desc))
                .findAny().orElseThrow(() -> new SrcMethodNotFound(srcMethod, existingMethod));

        cloneAndApplyLambdaRedirects(srcClass, targetClass, srcMethodNode, redirects, debugLogging);

        String dstMethodDescriptor = applyTransformsToMethodDescriptor(srcMethodNode.desc, redirects);

        MethodNode existingMethodNode = removeExistingMethod(targetClass, dstMethodName, dstMethodDescriptor);
        if (existingMethodNode != null && (existingMethodNode.access & ACC_NATIVE) == 0) {
            LOGGER.debug("Method transform overwriting existing method " + dstMethodName + " " + dstMethodDescriptor);
        }
        // FIXME: transform exceptions
        MethodNode dstMethodNode = new MethodNode(srcMethodNode.access, dstMethodName, dstMethodDescriptor, null, srcMethodNode.exceptions.toArray(new String[0]));

        srcMethodNode.accept(dasmTransformingVisitor(dstMethodNode, redirects, mappingsProvider));

        dstMethodNode.name = dstMethodName;

        targetClass.methods.add(dstMethodNode);
        return dstMethodNode;
    }

    /**
     * Apply all dasm transforms to a method body
     */
    private static MethodVisitor dasmTransformingVisitor(MethodVisitor visitor, TransformRedirects redirects, MappingsProvider mappingsProvider) {
        // FIXME: line numbers
        visitor = new Interfacicitifier(visitor, redirects);
        visitor = new MethodRemapper(visitor, new TypeRemapper(redirects.typeRedirects(), false, mappingsProvider));
        visitor = new RedirectVisitor(visitor, redirects, mappingsProvider);
        visitor = new ConstructorToFactoryBufferingVisitor(visitor, redirects);
        return visitor;
    }

    private static String applyTransformsToMethodDescriptor(String methodDescriptor, TransformRedirects redirects) {
        Type[] parameterTypes = Type.getArgumentTypes(methodDescriptor);
        Type returnType = Type.getReturnType(methodDescriptor);

        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i].getSort() == Type.OBJECT) {
                TypeAndIsInterface type = redirects.typeRedirects().get(parameterTypes[i]);
                parameterTypes[i] = type != null ? type.type() : parameterTypes[i];
            }
        }

        if (returnType.getSort() == Type.OBJECT) {
            returnType = redirects.typeRedirects().getOrDefault(returnType, new TypeAndIsInterface(returnType, false)).type();
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
                    new MethodRedirectImpl(classMethodLambda, targetClassType, lambdaRedirects.get(handle),
                            (handle.getTag() & H_INVOKESTATIC) != 0,
                            (targetClass.access & ACC_INTERFACE) != 0
                    )
            );
        }
    }

    private void applyRedirects(ClassNode srcClass, ClassMethod srcMethod, TransformRedirects redirects,
                                boolean debugLogging) throws SrcMethodNotFound {
        Method existingMethod = srcMethod.remap(this.mappingsProvider).method();

        MethodNode originalMethod = srcClass.methods.stream()
                .filter(method -> existingMethod.getName().equals(method.name) && existingMethod.getDescriptor().equals(method.desc))
                .findAny().orElseThrow(() -> new SrcMethodNotFound(srcMethod, existingMethod));

        applyLambdaRedirects(srcClass, originalMethod, redirects, debugLogging);

        String dstMethodDescriptor = applyTransformsToMethodDescriptor(originalMethod.desc, redirects);

        MethodNode existingMethodNode = removeExistingMethod(srcClass, originalMethod.name, dstMethodDescriptor);
        if (existingMethodNode != null && (existingMethodNode.access & ACC_NATIVE) == 0 && !originalMethod.desc.equals(dstMethodDescriptor)) {
            // in-place transforms must be overwriting a different method than the src one to log (otherwise every in-place transform would log)
            LOGGER.debug("Method transform overwriting existing method " + originalMethod.name + " " + dstMethodDescriptor);
        }

        MethodNode dstMethodNode = new MethodNode(originalMethod.access, originalMethod.name, dstMethodDescriptor, null, originalMethod.exceptions.toArray(new String[0]));
        originalMethod.accept(
                dasmTransformingVisitor(dstMethodNode, redirects, mappingsProvider)
        );
        srcClass.methods.remove(originalMethod);
        srcClass.methods.add(dstMethodNode);
    }

    private void applyLambdaRedirects(ClassNode srcClass, MethodNode method, TransformRedirects redirects,
                                      boolean debugLogging) throws SrcMethodNotFound {
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
                        applyRedirects(
                                srcClass,
                                new ClassMethod(Type.getObjectType(handle.getOwner()), new Method(name, desc)),
                                redirects,
                                debugLogging
                        );
                    }
                }
            }
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
            super(
                    "Could not find source method: `" + method.method().getName() + method.method().getDescriptor() +
                            "` | remapped: `" + remappedMethod.getName() + remappedMethod.getDescriptor() + "`"
            );
        }
    }
}
