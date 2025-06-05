package io.github.notstirred.dasm.annotation;

import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddFieldToSetsImpl;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddMethodToSetsImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.data.DasmContext;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmExceptionData;
import io.github.notstirred.dasm.exception.wrapped.DasmFieldExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmMethodExceptions;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.TypeUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.*;

public class AnnotationParser {
    private final ClassNodeProvider provider;
    /**
     * Nothing should ever put directly to either of these maps, instead use {@link #addRedirectSet(Type, RedirectSetImpl)}
     */
    private final Map<Type, RedirectSetImpl> redirectSetsByType = new HashMap<>();
    private final Map<Type, RedirectSetImpl.Container> containers = new HashMap<>();

    public AnnotationParser(ClassNodeProvider provider) {
        this.provider = provider;
    }

    public DasmContext parseDasmClasses(Collection<Class<?>> dasmClasses) throws DasmException {
        List<ClassNode> collect = new ArrayList<>();
        for (Class<?> clazz : dasmClasses) {
            ClassNode classNode = provider.classNode(Type.getType(clazz));
            collect.add(classNode);
        }
        return parseDasmClassNodes(collect);
    }

    public DasmContext parseDasmClassNodes(Collection<ClassNode> dasmClasses) throws DasmException {
        for (ClassNode dasmClass : dasmClasses) {
            findDasmAnnotations(dasmClass);
        }

        return new DasmContext(redirectSetsByType, containers);
    }

    private void addRedirectSet(Type redirectSetType, RedirectSetImpl redirectSet) {
        this.redirectSetsByType.put(redirectSetType, redirectSet);

        redirectSet.containers().forEach(container -> {
            this.containers.put(container.type(), container);
        });
    }

    private void findDasmAnnotations(ClassNode targetClass) throws DasmException {
        Type targetClassType = Type.getType(TypeUtil.typeNameToDescriptor(targetClass.name));
        boolean isTargetInterface = (targetClass.access & Opcodes.ACC_INTERFACE) != 0;

        DasmClassExceptions classExceptions = new DasmClassExceptions("An exception occurred when finding used redirect sets in", targetClass);

        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, Dasm.class, "value", classExceptions);
        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, TransformFromClass.class, "sets", classExceptions);

        for (FieldNode fieldNode : targetClass.fields) {
            DasmFieldExceptions fieldExceptions = classExceptions.addNested(new DasmFieldExceptions(fieldNode));
            findOuterRedirectSetsForAnnotation(fieldNode.invisibleAnnotations, AddFieldToSets.class, "containers", fieldExceptions);

            try {
                AddFieldToSetsImpl.parse(targetClassType, fieldNode).ifPresent(containersRedirectPair -> {
                    // All containers for this method must already exist, so we can just use the map
                    containersRedirectPair.first().forEach(containerType -> this.containers.get(containerType).fieldRedirects().add(containersRedirectPair.second()));
                });
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.InvalidMethodSignature |
                     MethodSigImpl.EmptySrcName e) {
                fieldExceptions.addException(e);
            }
        }

        for (MethodNode methodNode : targetClass.methods) {
            DasmMethodExceptions methodExceptions = classExceptions.addNested(new DasmMethodExceptions(methodNode));
            findRedirectSetsForAnnotation(methodNode.invisibleAnnotations, TransformFromMethod.class, "useRedirectSets", methodExceptions);
            findOuterRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddTransformToSets.class, "value", methodExceptions);
            findOuterRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddMethodToSets.class, "containers", methodExceptions);

            try {
                AddMethodToSetsImpl.parse(targetClassType, isTargetInterface, methodNode).ifPresent(containersRedirectPair -> {
                    // All containers for this method must already exist, so we can just use the map
                    containersRedirectPair.first().forEach(container -> this.containers.get(container).methodRedirects().add(containersRedirectPair.second()));
                });
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.InvalidMethodSignature |
                     MethodSigImpl.EmptySrcName e) {
                methodExceptions.addException(e);
            }
        }

        classExceptions.throwIfHasWrapped();
    }

    private void findRedirectSetsForAnnotation(List<AnnotationNode> annotations, Class<?> annotationClass, String setsAnnotationField,
                                               DasmExceptionData exceptions) {
        AnnotationNode annotationNode = getAnnotationIfPresent(annotations, annotationClass);
        if (annotationNode != null) {
            Map<String, Object> values = getAnnotationValues(annotationNode, annotationClass);
            @SuppressWarnings("unchecked") List<Type> sets = AnnotationUtil.<Type>annotationElementAsList(values.get(setsAnnotationField))
                    .orElseGet(ArrayList::new);
            for (Type redirectSetType : sets) {
                findRedirectSetsForType(redirectSetType, exceptions);
            }
        }
    }

    private void findRedirectSetsForType(Type redirectSetType, DasmExceptionData exceptions) {
        RedirectSetImpl existingSet = this.redirectSetsByType.get(redirectSetType);
        if (existingSet == null) {
            try {
                ClassNode redirectSetClass = this.provider.classNode(redirectSetType);
                Optional<RedirectSetImpl> parsed = RedirectSetImpl.parse(redirectSetClass, this.provider, exceptions);
                if (parsed.isPresent()) {
                    existingSet = parsed.get();
                    this.addRedirectSet(redirectSetType, existingSet);
                } else {
                    exceptions.addException(new NoValidRedirectSetExists(redirectSetType));
                    return;
                }
            } catch (NoSuchTypeExists e) {
                exceptions.addException(e);
                return;
            }
        }

        for (Type superRedirectSet : existingSet.superRedirectSets()) {
            findRedirectSetsForType(superRedirectSet, exceptions);
        }
    }

    private void findOuterRedirectSetsForAnnotation(List<AnnotationNode> annotations, Class<?> annotationClass, String containersAnnotationField,
                                                    DasmExceptionData exceptions) {
        AnnotationNode annotationNode = getAnnotationIfPresent(annotations, annotationClass);
        if (annotationNode != null) {
            Map<String, Object> values = getAnnotationValues(annotationNode, annotationClass);
            @SuppressWarnings("unchecked") List<Type> containers = AnnotationUtil.<Type>annotationElementAsList(values.get(containersAnnotationField))
                    .orElseGet(ArrayList::new);
            for (Type redirectSetType : containers) {
                findOuterRedirectSetsForType(redirectSetType, exceptions);
            }
        }
    }

    private void findOuterRedirectSetsForType(Type containerType, DasmExceptionData exceptions) {
        RedirectSetImpl.Container existingContainer = this.containers.get(containerType);
        if (existingContainer != null) {
            return; // If the container exists we must've parsed its redirect set before.
        }

        RedirectSetImpl existingSet;
        try {
            ClassNode clazz = getContainingRedirectSetClassNode(containerType);

            Optional<RedirectSetImpl> parsed = RedirectSetImpl.parse(clazz, this.provider, exceptions);
            if (parsed.isPresent()) {
                existingSet = parsed.get();
                this.addRedirectSet(Type.getObjectType(clazz.name), existingSet);
            } else {
                exceptions.addException(new NoValidRedirectSetExists(containerType));
                return;
            }
        } catch (NoSuchTypeExists | ContainerNotWithinRedirectSet e) {
            exceptions.addException(e);
            return;
        }

        for (Type superRedirectSet : existingSet.superRedirectSets()) {
            findRedirectSetsForType(superRedirectSet, exceptions);
        }
    }

    /**
     * Walk up the outer classes until we find a ClassNode with a {@link RedirectSet} annotation
     */
    private ClassNode getContainingRedirectSetClassNode(Type containerType) throws NoSuchTypeExists, ContainerNotWithinRedirectSet {
        Type type = containerType;
        ClassNode clazz = this.provider.classNode(type);

        if (clazz.outerClass == null) {
            throw new ContainerNotWithinRedirectSet(containerType);
        }

        while (true) {
            type = Type.getObjectType(clazz.outerClass);
            clazz = this.provider.classNode(type);

            if (isAnnotationPresent(clazz.invisibleAnnotations, RedirectSet.class)) {
                return clazz;
            }

            if (clazz.outerClass == null) {
                throw new ContainerNotWithinRedirectSet(containerType);
            }
        }
    }

    public static class ContainerNotWithinRedirectSet extends DasmException {
        public ContainerNotWithinRedirectSet(Type containerType) {
            super(String.format("Container `" + containerType.getClassName() + "` must be within a @RedirectSet interface"));
        }
    }

    public static class NoValidRedirectSetExists extends DasmException {
        public NoValidRedirectSetExists(Type redirectSetType) {
            super(String.format("No valid redirect set exists matching `" + redirectSetType.getClassName() + "`"));
        }
    }
}
