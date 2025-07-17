package io.github.notstirred.dasm.annotation;

import io.github.notstirred.dasm.annotation.parse.DasmImpl;
import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddFieldToMethodToSetsImpl;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddFieldToSetsImpl;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddMethodToSetsImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.transform.TransformClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.data.DasmContext;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.notify.Notification;
import io.github.notstirred.dasm.util.*;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.*;
import static io.github.notstirred.dasm.util.Format.formatObjectType;

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

    @Deprecated
    public DasmContext buildContext() {
        return new DasmContext(this.redirectSetsByType, this.containers);
    }

    public Pair<DasmContext, List<Notification>> parseDasmClasses(Collection<Class<?>> dasmClasses) throws DasmException {
        List<ClassNode> collect = new ArrayList<>();
        for (Class<?> clazz : dasmClasses) {
            ClassNode classNode = provider.classNode(Type.getType(clazz));
            collect.add(classNode);
        }
        return parseDasmClassNodes(collect);
    }

    public Pair<DasmContext, List<Notification>> parseDasmClassNodes(Collection<ClassNode> dasmClasses) {
        NotifyStack combinedNotifications = dasmClasses.stream().map(this::findDasmAnnotations)
                .collect(NotifyStack.joining());

        if (combinedNotifications.hasError()) {
            return new Pair<>(null, combinedNotifications.notifications());
        }
        return new Pair<>(new DasmContext(redirectSetsByType, containers), combinedNotifications.notifications());
    }

    private void addRedirectSet(Type redirectSetType, RedirectSetImpl redirectSet) {
        this.redirectSetsByType.put(redirectSetType, redirectSet);

        redirectSet.containers().forEach(container -> this.containers.put(container.type(), container));
    }

    @Deprecated
    public NotifyStack findDasmAnnotations(ClassNode targetClass) {
        AnnotationNode dasmNode = getAnnotationIfPresent(targetClass.invisibleAnnotations, Dasm.class);
        Type targetClassType = DasmImpl.parse(dasmNode).target()
                .orElseGet(() -> Type.getType(TypeUtil.typeNameToDescriptor(targetClass.name)));

        boolean isTargetInterface = (targetClass.access & Opcodes.ACC_INTERFACE) != 0;

        NotifyStack classExceptions = NotifyStack.of(targetClass); // We report errors on the actual class, not the @Dasm#target

        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, Dasm.class, "value", classExceptions);
        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, TransformFromClass.class, "sets", classExceptions);
        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, TransformClass.class, "sets", classExceptions);

        for (FieldNode fieldNode : targetClass.fields) {
            NotifyStack fieldExceptions = classExceptions.push(fieldNode);
            findOuterRedirectSetsForAnnotation(fieldNode.invisibleAnnotations, AddFieldToSets.class, "containers", fieldExceptions);

            try {
                Optional<AddFieldToSetsImpl> optAddToSets = AddFieldToSetsImpl.parse(fieldNode);
                if (optAddToSets.isPresent()) {
                    AddFieldToSetsImpl addToSets = optAddToSets.get();
                    // All containers for this method must already exist, so we can just use the map
                    for (Type containerType : addToSets.containers()) {
                        RedirectSetImpl.Container container = this.containers.get(containerType);
                        if (container == null) {
                            fieldExceptions.notifyFromException(new ContainerNotWithinRedirectSet(containerType));
                            continue;
                        } else if (!container.dstType().equals(targetClassType)) {
                            fieldExceptions.notify(new AddToSetsContainerInvalidDstType(containerType, container.dstType(), targetClassType));
                            continue;
                        }

                        FieldRedirectImpl fieldRedirect = new FieldRedirectImpl(
                                new ClassField(container.srcType(), addToSets.mappingsOwner().orElse(container.srcType()), addToSets.srcField().type(), addToSets.srcField().name()),
                                container.dstType(),
                                addToSets.dstFieldName()
                        );
                        container.fieldRedirects().add(fieldRedirect);
                    }
                }
            } catch (RefImpl.RefAnnotationGivenNoArguments | ReferenceUtil.InvalidReference e) {
                fieldExceptions.notifyFromException(e);
            }
        }

        for (MethodNode methodNode : targetClass.methods) {
            NotifyStack methodExceptions = classExceptions.push(methodNode);
            findRedirectSetsForAnnotation(methodNode.invisibleAnnotations, TransformFromMethod.class, "useRedirectSets", methodExceptions);
            findOuterRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddTransformToSets.class, "value", methodExceptions);
            findOuterRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddMethodToSets.class, "containers", methodExceptions);

            try {
                Optional<AddMethodToSetsImpl> optAddToSets = AddMethodToSetsImpl.parse(isTargetInterface, methodNode);
                if (optAddToSets.isPresent()) {
                    AddMethodToSetsImpl addToSets = optAddToSets.get();
                    // All containers for this method must already exist, so we can just use the map
                    for (Type containerType : addToSets.containers()) {
                        RedirectSetImpl.Container container = this.containers.get(containerType);
                        if (container == null) {
                            methodExceptions.notifyFromException(new ContainerNotWithinRedirectSet(containerType));
                            continue;
                        } else if (!container.dstType().equals(targetClassType)) {
                            methodExceptions.notify(new AddToSetsContainerInvalidDstType(containerType, container.dstType(), targetClassType));
                            continue;
                        }

                        MethodRedirectImpl methodRedirect = new MethodRedirectImpl(
                                new ClassMethod(container.srcType(), addToSets.mappingsOwner().orElse(container.srcType()), addToSets.srcMethod()),
                                container.dstType(),
                                addToSets.dstMethodName(),
                                addToSets.isStatic(),
                                addToSets.isDstInterface()
                        );
                        container.methodRedirects().add(methodRedirect);
                    }
                }
            } catch (RefImpl.RefAnnotationGivenNoArguments | ReferenceUtil.InvalidReference e) {
                methodExceptions.notifyFromException(e);
            }

            try {
                Optional<AddFieldToMethodToSetsImpl> optAddToSets = AddFieldToMethodToSetsImpl.parse(methodNode);
                if (optAddToSets.isPresent()) {
                    AddFieldToMethodToSetsImpl addToSets = optAddToSets.get();
                    // All containers for this method must already exist, so we can just use the map
                    for (Type containerType : addToSets.containers()) {
                        RedirectSetImpl.Container container = this.containers.get(containerType);
                        if (container == null) {
                            methodExceptions.notifyFromException(new ContainerNotWithinRedirectSet(containerType));
                            continue;
                        } else if (!container.dstType().equals(targetClassType)) {
                            methodExceptions.notify(new AddToSetsContainerInvalidDstType(containerType, container.dstType(), targetClassType));
                            continue;
                        }

                        FieldToMethodRedirectImpl fieldToMethodRedirect = new FieldToMethodRedirectImpl(
                                new ClassField(container.srcType(), addToSets.mappingsOwner().orElse(container.srcType()), addToSets.srcField().type(), addToSets.srcField().name()),
                                new ClassMethod(container.dstType(), addToSets.dstMethod()),
                                addToSets.dstSetterMethod().map(method -> new ClassMethod(container.dstType(), method)),
                                addToSets.isStatic(),
                                (targetClass.access & Opcodes.ACC_INTERFACE) != 0
                        );
                        container.fieldToMethodRedirects().add(fieldToMethodRedirect);
                    }
                }
            } catch (RefImpl.RefAnnotationGivenNoArguments | ReferenceUtil.InvalidReference e) {
                methodExceptions.notifyFromException(e);
            }
        }
        return classExceptions;
    }

    private void findRedirectSetsForAnnotation(List<AnnotationNode> annotations, Class<?> annotationClass, String setsAnnotationField,
                                               NotifyStack exceptions) {
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

    private void findRedirectSetsForType(Type redirectSetType, NotifyStack exceptions) {
        RedirectSetImpl existingSet = this.redirectSetsByType.get(redirectSetType);
        if (existingSet == null) {
            try {
                ClassNode redirectSetClass = this.provider.classNode(redirectSetType);
                Optional<RedirectSetImpl> parsed = RedirectSetImpl.parse(redirectSetClass, this.provider, exceptions);
                if (parsed.isPresent()) {
                    existingSet = parsed.get();
                    this.addRedirectSet(redirectSetType, existingSet);
                } else {
                    exceptions.notify(new NoValidRedirectSetExists(redirectSetType));
                    return;
                }
            } catch (NoSuchTypeExists e) {
                exceptions.notifyFromException(e);
                return;
            }
        }

        for (Type superRedirectSet : existingSet.superRedirectSets()) {
            findRedirectSetsForType(superRedirectSet, exceptions);
        }
    }

    private void findOuterRedirectSetsForAnnotation(List<AnnotationNode> annotations, Class<?> annotationClass, String containersAnnotationField,
                                                    NotifyStack exceptions) {
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

    private void findOuterRedirectSetsForType(Type containerType, NotifyStack exceptions) {
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
                exceptions.notify(new NoValidRedirectSetExists(containerType));
                return;
            }
        } catch (NoSuchTypeExists | ContainerNotWithinRedirectSet | TypeIsNotAContainer e) {
            exceptions.notifyFromException(e);
            return;
        }

        for (Type superRedirectSet : existingSet.superRedirectSets()) {
            findRedirectSetsForType(superRedirectSet, exceptions);
        }
    }

    /**
     * Walk up the outer classes until we find a ClassNode with a {@link RedirectSet} annotation
     */
    private ClassNode getContainingRedirectSetClassNode(Type containerType) throws NoSuchTypeExists, ContainerNotWithinRedirectSet, TypeIsNotAContainer {
        Type type = containerType;
        ClassNode clazz = this.provider.classNode(type);

        if (!(isAnnotationPresent(clazz.invisibleAnnotations, TypeRedirect.class)
                || isAnnotationPresent(clazz.invisibleAnnotations, InterOwnerContainer.class)
                || isAnnotationPresent(clazz.invisibleAnnotations, IntraOwnerContainer.class))) {
            throw new TypeIsNotAContainer(type);
        }

        while (true) {
            String outerClass = ClassNodeUtil.outerClass(clazz);
            if (outerClass == null) {
                throw new ContainerNotWithinRedirectSet(containerType);
            }
            type = Type.getObjectType(outerClass);
            clazz = this.provider.classNode(type);

            if (isAnnotationPresent(clazz.invisibleAnnotations, RedirectSet.class)) {
                return clazz;
            }
        }
    }

    public static class ContainerNotWithinRedirectSet extends DasmException {
        public ContainerNotWithinRedirectSet(Type containerType) {
            super(String.format("Container `" + formatObjectType(containerType) + "` must be within a @RedirectSet interface"));
        }
    }

    public static class TypeIsNotAContainer extends DasmException {
        public TypeIsNotAContainer(Type type) {
            super(String.format("Type `" + formatObjectType(type) + "` is not a container but is used as one"));
        }
    }

    public static class AddToSetsContainerInvalidDstType extends Notification {
        public AddToSetsContainerInvalidDstType(Type containerType, Type containerDstType, Type dasmTargetType) {
            super(String.format("addToSet attempts to add to a Container `" + formatObjectType(containerType) + "` with a `to` type of " + formatObjectType(containerDstType) + " but its dasm target is " + formatObjectType(dasmTargetType)));
        }
    }

    public static class NoValidRedirectSetExists extends Notification {
        public NoValidRedirectSetExists(Type redirectSetType) {
            super(String.format("No valid redirect set exists matching `" + formatObjectType(redirectSetType) + "`"));
        }
    }
}
