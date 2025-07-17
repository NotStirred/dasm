package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.annotation.parse.AddedParameter;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.annotations.transform.Visibility;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.notify.Notification;
import io.github.notstirred.dasm.transformer.data.*;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.NotifyStack;
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

import java.util.*;

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
        ClassNode sourceClass;
        if (transform.srcType().equals(transform.dstType())) { // inplace transform
            sourceClass = new ClassNode();
            targetClass.accept(sourceClass);
        } else {
            sourceClass = classNodeProvider.classNode(transform.srcType());
        }

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
            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                String redirectedDescriptor = builtRedirects.typeRedirectsDescriptors().getOrDefault(descriptor, descriptor);
                String key = sourceClass.name + "." + name;

                FieldRedirectImpl fieldRedirect = builtRedirects.fieldRedirects().get(key);
                if (fieldRedirect == null) {
                    return super.visitField(access, name, redirectedDescriptor, signature, value);
                } else {
                    return super.visitField(access, fieldRedirect.dstName(), redirectedDescriptor, null, value);
                }
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                String key = sourceClass.name + "." + name + descriptor;
                // here we manually get the redirect, as the remappers only look within a method, not at the definition
                MethodRedirectImpl methodRedirect = builtRedirects.methodRedirects().get(key);
                String dstName = methodRedirect == null ? name : methodRedirect.dstName();

                String redirectedDescriptor = applyTransformsToMethodDescriptor(descriptor, redirects, Collections.emptyList());

                MethodVisitor visitor = super.visitMethod(access, dstName, redirectedDescriptor, signature, exceptions);
                assert visitor instanceof MethodNode; // We assume that we receive a MethodNode (ClassNode always returns one)
                return dasmTransformingVisitor(
                        redirectedDescriptor,
                        Type.getObjectType(targetClass.name),
                        descriptor,
                        (MethodNode) visitor,
                        redirects,
                        mappingsProvider,
                        Collections.emptyList()
                );
            }
        };
        sourceClass.accept(cv);
        oldNode.accept(cv);
    }

    public List<Notification> transform(ClassNode targetClass, Collection<MethodTransform> transforms) {
        NotifyStack dasmClassExceptions = NotifyStack.of(targetClass);

        Type targetClassType = Type.getType(TypeUtil.typeNameToDescriptor(targetClass.name));
        for (MethodTransform transform : transforms) {
            NotifyStack methodExceptions = dasmClassExceptions.push(transform.originalTransformData().methodNode());
            Type methodSrcOwner = transform.srcMethod().owner();

            ClassNode srcClass;
            if (methodSrcOwner.equals(targetClassType)) {
                srcClass = targetClass;
            } else {
                try {
                    srcClass = this.classNodeProvider.classNode(methodSrcOwner);
                } catch (NoSuchTypeExists e) {
                    methodExceptions.notifyFromException(e);
                    continue;
                }
            }

            TransformRedirects transformRedirects = new TransformRedirects(transform.redirectSets(), this.mappingsProvider);
            if (transform.inPlace()) {
                applyRedirects(srcClass, transform.srcMethod(), transformRedirects, transform.transformChanges(), transform.originalTransformData(), methodExceptions, true);
            } else {
                // FIXME: java 8 synthetic accessor methods
                cloneAndApplyRedirects(srcClass, targetClass, transform.srcMethod(), transform.dstMethodName(), transformRedirects, transform.transformChanges(), transform.originalTransformData(), methodExceptions, true);
            }
        }

        return dasmClassExceptions.notifications();
    }

    /**
     * @param redirects lambda redirects are implicitly added, so the parameter is modified.
     */
    private Optional<MethodNode> cloneAndApplyRedirects(ClassNode srcClass, ClassNode targetClass,
                                                        ClassMethod srcMethod, String dstMethodName,
                                                        TransformRedirects redirects,
                                                        MethodTransform.TransformChanges transformChanges,
                                                        MethodTransform.OriginalTransformData originalTransformData,
                                                        NotifyStack methodExceptions,
                                                        boolean debugLogging) {
        return redirects(srcClass, targetClass, srcMethod, dstMethodName, redirects, transformChanges, originalTransformData, methodExceptions, debugLogging,
                (srcMethodNode, dstMethodNode, existingMethodNode) -> {
                    cloneAndApplyLambdaRedirects(srcClass, targetClass, srcMethodNode, redirects, methodExceptions, debugLogging);
                    if (debugLogging && existingMethodNode != null && (existingMethodNode.access & ACC_NATIVE) == 0) {
                        LOGGER.debug("Method transform overwriting existing method " + dstMethodNode.name + " " + dstMethodNode.desc);
                    }
                    dstMethodNode.name = dstMethodName;
                    targetClass.methods.add(dstMethodNode);
                }
        );
    }

    private void applyRedirects(ClassNode srcClass, ClassMethod srcMethod,
                                TransformRedirects redirects,
                                MethodTransform.TransformChanges transformChanges,
                                MethodTransform.OriginalTransformData originalTransformData,
                                NotifyStack methodExceptions,
                                boolean debugLogging) {
        redirects(srcClass, srcClass, srcMethod, srcMethod.method().getName(), redirects, transformChanges, originalTransformData, methodExceptions, debugLogging,
                (srcMethodNode, dstMethodNode, existingMethodNode) -> {
                    applyLambdaRedirects(srcClass, srcMethodNode, redirects, methodExceptions, debugLogging);
                    if (debugLogging && existingMethodNode != null && (existingMethodNode.access & ACC_NATIVE) == 0 && !srcMethodNode.desc.equals(dstMethodNode.desc)) {
                        // in-place transforms must be overwriting a different method than the src one to log (otherwise every in-place transform would log)
                        LOGGER.debug("Method transform overwriting existing method " + srcMethodNode.name + " " + dstMethodNode.desc);
                    }
                    srcClass.methods.remove(srcMethodNode);
                    srcClass.methods.add(dstMethodNode);
                }
        );
    }

    @FunctionalInterface
    interface RedirectsFunction {
        void call(MethodNode srcMethodNode, MethodNode dstMethodNode, @Nullable MethodNode existingMethodNode);
    }

    /**
     * @return Returns empty if the source method could not be found
     */
    private Optional<MethodNode> redirects(ClassNode srcClass, ClassNode targetClass,
                                           ClassMethod srcMethod, String dstMethodName,
                                           TransformRedirects redirects,
                                           MethodTransform.TransformChanges transformChanges,
                                           MethodTransform.OriginalTransformData originalTransformData,
                                           NotifyStack methodExceptions,
                                           boolean debugLogging,
                                           RedirectsFunction f) {
        Method existingMethod = srcMethod.remap(this.mappingsProvider).method();

        Optional<MethodNode> srcMethodNodeOptional = srcClass.methods.stream()
                .filter(method -> existingMethod.getName().equals(method.name) && existingMethod.getDescriptor().equals(method.desc))
                .findAny();
        if (!srcMethodNodeOptional.isPresent()) {
            methodExceptions.notify(new SrcMethodNotFound(srcMethod, existingMethod));
            return Optional.empty();
        }
        MethodNode srcMethodNode = srcMethodNodeOptional.get();

        String dstMethodDescriptor = applyTransformsToMethodDescriptor(srcMethodNode.desc, redirects, transformChanges.addedParameters());
        MethodNode existingMethodNode = removeExistingMethod(targetClass, dstMethodName, dstMethodDescriptor);

        transformChanges.checkAccess(originalTransformData, Visibility.fromAccess(srcMethodNode.access), methodExceptions);
        if (methodExceptions.hasError()) {
            return Optional.empty();
        }

        // FIXME: transform exceptions
        int access = transformChanges.dstMethodVisibility().access | (srcMethodNode.access & ~(ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE));
        MethodNode dstMethodNode = new MethodNode(access, dstMethodName, dstMethodDescriptor, null, srcMethodNode.exceptions.toArray(new String[0]));

        f.call(srcMethodNode, dstMethodNode, existingMethodNode);

        srcMethodNode.accept(dasmTransformingVisitor(dstMethodDescriptor, Type.getObjectType(targetClass.name), srcMethodNode.desc, dstMethodNode, redirects, mappingsProvider, transformChanges.addedParameters()));
        return Optional.of(dstMethodNode);
    }

    /**
     * Apply all dasm transforms to a method body
     */
    private static MethodVisitor dasmTransformingVisitor(String newMethodDescriptor, Type owner, String originalMethodDesc, MethodNode node,
                                                         TransformRedirects redirects, MappingsProvider mappingsProvider,
                                                         List<AddedParameter> addedParameters) {
        // FIXME: line numbers
        MethodVisitor visitor = new ParameterAdder(node, owner, originalMethodDesc, newMethodDescriptor, addedParameters);
        visitor = new Interfacicitifier(visitor, redirects);
        visitor = new MethodRemapper(visitor, new TypeRemapper(redirects.typeRedirects(), false, mappingsProvider));
        visitor = new RedirectVisitor(visitor, redirects, mappingsProvider);
        visitor = new ConstructorToFactoryBufferingVisitor(visitor, redirects);
        return visitor;
    }

    private static String applyTransformsToMethodDescriptor(String methodDescriptor, TransformRedirects redirects,
                                                            List<AddedParameter> addedParameters) {
        Type[] parameterTypes = Type.getArgumentTypes(methodDescriptor);
        Type returnType = Type.getReturnType(methodDescriptor);

        for (int i = 0; i < parameterTypes.length; i++) {
            parameterTypes[i] = redirectType(parameterTypes[i], redirects);
        }

        returnType = redirectType(returnType, redirects);

        List<Type> parameterTypeList = new ArrayList<>(Arrays.asList(parameterTypes));

        // add parameters such that indices are always with respect to the original method signature and do not change as parameters are added.
        addedParameters.stream().sorted(Comparator.comparingInt(AddedParameter::index))
                .forEachOrdered(addedParameter -> {
                    parameterTypeList.add(addedParameter.index(), addedParameter.type());
                });

        return Type.getMethodDescriptor(returnType, parameterTypeList.toArray(new Type[0]));
    }

    private static Type redirectType(Type parameterType, TransformRedirects redirects) {
        if (parameterType.getSort() == Type.ARRAY) {
            TypeAndIsInterface type = redirects.typeRedirects().get(parameterType.getElementType());
            String arrayPart = String.join("", Collections.nCopies(parameterType.getDimensions(), "["));
            parameterType = type != null ? Type.getType(arrayPart + type.type()) : parameterType;
        } else {
            TypeAndIsInterface type = redirects.typeRedirects().get(parameterType);
            parameterType = type != null ? type.type() : parameterType;
        }
        return parameterType;
    }

    private void cloneAndApplyLambdaRedirects(ClassNode srcClass, ClassNode targetClass, MethodNode method, TransformRedirects redirects,
                                              NotifyStack methodExceptions, boolean debugLogging) {
        Map<Handle, String> lambdaRedirects = new HashMap<>();

        forEachLambdaInvocation(srcClass, method, (classMethod, handle, lambdaNode) -> {
            String newName = "dasm$redirect$" + classMethod.method().getName();
            lambdaRedirects.put(handle, newName);
            NotifyStack lambdaExceptions = methodExceptions.push(lambdaNode);
            cloneAndApplyRedirects(
                    srcClass,
                    targetClass,
                    classMethod,
                    newName,
                    redirects,
                    new MethodTransform.TransformChanges(Collections.emptyList(), Visibility.PRIVATE, Visibility.SAME_AS_TARGET),
                    new MethodTransform.OriginalTransformData(srcClass.name, method),
                    lambdaExceptions,
                    debugLogging
            );
        });

        Type targetClassType = Type.getType(TypeUtil.typeNameToDescriptor(targetClass.name));
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

    private void applyLambdaRedirects(ClassNode srcClass, MethodNode method, TransformRedirects redirects,
                                      NotifyStack methodExceptions, boolean debugLogging) {
        forEachLambdaInvocation(srcClass, method, (classMethod, handle, lambdaNode) -> {
            NotifyStack lambdaExceptions = methodExceptions.push(lambdaNode);
            applyRedirects(
                    srcClass,
                    classMethod,
                    redirects,
                    new MethodTransform.TransformChanges(Collections.emptyList(), Visibility.PRIVATE, Visibility.SAME_AS_TARGET),
                    new MethodTransform.OriginalTransformData(srcClass.name, method), // Assume lambas are always private, the passed in method here is therefore irrelevant
                    lambdaExceptions,
                    debugLogging
            );
        });
    }

    @FunctionalInterface
    interface RedirectLambdaFunction {
        void call(ClassMethod srcMethod, Handle handle, MethodNode lambdaMethodNode);
    }

    private void forEachLambdaInvocation(ClassNode srcClass, MethodNode method, RedirectLambdaFunction f) {
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

                        f.call(new ClassMethod(Type.getObjectType(handle.getOwner()), new Method(name, desc)), handle, targetNode);
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
    public static class SrcMethodNotFound extends Notification {
        public SrcMethodNotFound(ClassMethod method, Method remappedMethod) {
            super(
                    "Could not find source method: `" + method.method().getName() + method.method().getDescriptor() +
                            "` | remapped: `" + remappedMethod.getName() + remappedMethod.getDescriptor() + "`"
            );
        }
    }
}
