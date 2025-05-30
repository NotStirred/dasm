package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.parse.redirects.*;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl.FieldMissingFieldRedirectAnnotationException;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmExceptionData;
import io.github.notstirred.dasm.exception.wrapped.DasmFieldExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmMethodExceptions;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.TypeUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationIfPresent;
import static io.github.notstirred.dasm.util.TypeUtil.simpleClassNameOf;
import static io.github.notstirred.dasm.util.Util.atLeastTwoOf;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

@Getter
public class RedirectSetImpl {
    private final List<Type> superRedirectSets;

    private final Set<FieldToMethodRedirectImpl> fieldToMethodRedirects;
    private final Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects;
    private final Set<FieldRedirectImpl> fieldRedirects;
    private final Set<MethodRedirectImpl> methodRedirects;
    private final Set<TypeRedirectImpl> typeRedirects;

    public RedirectSetImpl(List<Type> superRedirectSets, Set<FieldToMethodRedirectImpl> fieldToMethodRedirects,
                           Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects, Set<FieldRedirectImpl> fieldRedirects,
                           Set<MethodRedirectImpl> methodRedirects, Set<TypeRedirectImpl> typeRedirects) {
        this.superRedirectSets = superRedirectSets;
        this.fieldToMethodRedirects = fieldToMethodRedirects;
        this.constructorToFactoryRedirects = constructorToFactoryRedirects;
        this.fieldRedirects = fieldRedirects;
        this.methodRedirects = methodRedirects;
        this.typeRedirects = typeRedirects;
    }

    public static Optional<RedirectSetImpl> parse(ClassNode redirectSetClassNode, ClassNodeProvider provider, DasmExceptionData exceptions) {
        List<Type> superRedirectSets = new ArrayList<>();

        DasmClassExceptions redirectSetExceptions = new DasmClassExceptions("An exception occurred when parsing redirect set", redirectSetClassNode);

        AnnotationNode annotationNode = getAnnotationIfPresent(redirectSetClassNode.invisibleAnnotations, RedirectSet.class);
        if (annotationNode == null) {
            redirectSetExceptions.addException(new MissingRedirectSetAnnotationException(Type.getObjectType(redirectSetClassNode.name)));
        }

        if ((redirectSetClassNode.access & ACC_INTERFACE) == 0) {
            redirectSetExceptions.addException(new NonInterfaceIsUsedAsRedirectSetException(Type.getObjectType(redirectSetClassNode.name)));
        }

        // Add inherited redirect sets
        for (String itf : redirectSetClassNode.interfaces) {
            superRedirectSets.add(Type.getObjectType(itf));
        }

        Map<String, InnerClassParsedData> innerClassData = new HashMap<>();

        // Discover type/field/method redirects in innerClass
        for (InnerClassNode innerClass : redirectSetClassNode.innerClasses) {
            if (!innerClass.outerName.equals(redirectSetClassNode.name) || innerClass.name.equals(redirectSetClassNode.name)) {
                // `innerClasses` contains a list of all inner classes of the root class, exclude any not a direct child
                //                also seems to contain the outer class too.
                continue;
            }

            ClassNode innerClassNode;
            try {
                innerClassNode = provider.classNode(Type.getObjectType(innerClass.name));
            } catch (NoSuchTypeExists e) {
                redirectSetExceptions.addException(e);
                // The inner class doesn't exist, we can't begin parsing it.
                continue;
            }
            DasmClassExceptions innerClassExceptions = redirectSetExceptions.addNested(new DasmClassExceptions(
                    "An exception occurred when parsing inner class of redirect set", innerClassNode));

            parseInnerClass(innerClassNode, innerClassExceptions).ifPresent(data -> innerClassData.put(innerClass.name, data));
        }

        List<Node<InnerClassParsedData>> roots = createTopDownTree(innerClassData);

        roots.forEach(root ->
                root.children.forEach(child ->
                        createInheritedRedirectsForTree(root.value, child)));

        Set<FieldToMethodRedirectImpl> fieldToMethodRedirects = new HashSet<>();
        Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects = new HashSet<>();
        Set<FieldRedirectImpl> fieldRedirects = new HashSet<>();
        Set<MethodRedirectImpl> methodRedirects = new HashSet<>();
        Set<TypeRedirectImpl> typeRedirects = new HashSet<>();

        innerClassData.forEach((name, data) -> {
            fieldToMethodRedirects.addAll(data.fieldToMethodRedirects);
            constructorToFactoryRedirects.addAll(data.constructorToFactoryRedirects);
            fieldRedirects.addAll(data.fieldRedirects);
            methodRedirects.addAll(data.methodRedirects);
            typeRedirects.addAll(data.typeRedirects);
        });

        if (redirectSetExceptions.hasWrapped()) {
            exceptions.addNested(redirectSetExceptions);
            return Optional.empty();
        }

        return Optional.of(new RedirectSetImpl(superRedirectSets, fieldToMethodRedirects, constructorToFactoryRedirects, fieldRedirects, methodRedirects, typeRedirects));
    }

    /**
     * Takes all of the inner classes of a redirect set and arranges them in a tree by their class hierarchy
     */
    private static @NotNull List<Node<InnerClassParsedData>> createTopDownTree(Map<String, InnerClassParsedData> innerClassData) {
        List<Node<InnerClassParsedData>> roots = new ArrayList<>();
        List<Node<InnerClassParsedData>> remaining = new ArrayList<>();
        Map<String, Node<InnerClassParsedData>> nodes = new HashMap<>();

        // Create initial data
        innerClassData.forEach((name, data) -> {
            Node<InnerClassParsedData> node = new Node<>(data);
            nodes.put(name, node);
            if (data.superDataName == null || data.superDataName.equals(TypeUtil.classToInternalName(Object.class))) {
                roots.add(node);
            } else {
                remaining.add(node);
            }
        });

        // Arrange into tree
        while (!remaining.isEmpty()) {
            for (Iterator<Node<InnerClassParsedData>> iterator = remaining.iterator(); iterator.hasNext(); ) {
                Node<InnerClassParsedData> node = iterator.next();

                Node<InnerClassParsedData> superNode = nodes.get(node.value.superDataName);
                if (superNode != null) {
                    superNode.children.add(node);
                    iterator.remove();
                }
            }
        }
        return roots;
    }

    private static void createInheritedRedirectsForTree(InnerClassParsedData parent, Node<InnerClassParsedData> data) {
        // FIXME: how should mappings owner interact with inheritance changing the owner?
        //        is the current approach fine? the mappings owner never gets changed assuming it's a super type. feels weird but maybe correct.
        OwnerChanger ownerChanger = new OwnerChanger(
                parent.srcType.getClassName(), data.value.srcType.getClassName(),
                parent.dstType.getClassName(), data.value.dstType.getClassName()
        );
        parent.fieldToMethodRedirects.stream().map(ownerChanger::remap).forEach(r -> data.value.fieldToMethodRedirects.add(r));
        parent.constructorToFactoryRedirects.stream().map(ownerChanger::remap).forEach(r -> data.value.constructorToFactoryRedirects.add(r));
        parent.fieldRedirects.stream().map(ownerChanger::remap).forEach(r -> data.value.fieldRedirects.add(r));
        parent.methodRedirects.stream().map(ownerChanger::remap).forEach(r -> data.value.methodRedirects.add(r));
        parent.typeRedirects.stream().map(ownerChanger::remap).forEach(r -> data.value.typeRedirects.add(r));

        data.value.fieldToMethodRedirects.addAll(parent.fieldToMethodRedirects);
        data.value.constructorToFactoryRedirects.addAll(parent.constructorToFactoryRedirects);
        data.value.fieldRedirects.addAll(parent.fieldRedirects);
        data.value.methodRedirects.addAll(parent.methodRedirects);
        data.value.typeRedirects.addAll(parent.typeRedirects);

        data.children.forEach(child -> createInheritedRedirectsForTree(data.value, child));
    }

    @RequiredArgsConstructor
    private static class Node<T> {
        final T value;
        final List<Node<T>> children = new ArrayList<>();
    }

    private static class InnerClassParsedData {
        Type srcType;
        Type dstType;

        String superDataName;

        Set<FieldToMethodRedirectImpl> fieldToMethodRedirects = new HashSet<>();
        Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects = new HashSet<>();
        Set<FieldRedirectImpl> fieldRedirects = new HashSet<>();
        Set<MethodRedirectImpl> methodRedirects = new HashSet<>();
        Set<TypeRedirectImpl> typeRedirects = new HashSet<>();
    }

    private static Optional<InnerClassParsedData> parseInnerClass(ClassNode innerClassNode, DasmClassExceptions innerClassExceptions) {
        InnerClassParsedData data = new InnerClassParsedData();

        Optional<TypeRedirectImpl> typeRedirect = TypeRedirectImpl.parse(innerClassNode, innerClassExceptions);
        Optional<InterOwnerContainerImpl> interOwnerContainer = InterOwnerContainerImpl.parse(innerClassNode, innerClassExceptions);
        Optional<IntraOwnerContainerImpl> intraOwnerContainer = IntraOwnerContainerImpl.parse(innerClassNode, innerClassExceptions);

        if (innerClassExceptions.hasWrapped()) {
            return Optional.empty();
        }

        if (!(typeRedirect.isPresent() | interOwnerContainer.isPresent() | intraOwnerContainer.isPresent())) {
            // The inner class must have one of @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer, but does not.
            innerClassExceptions.addException(new MissingContainerException(Type.getObjectType(innerClassNode.name)));
            // We don't know what the src/dst owners are, we can't continue parsing this inner class.
            return Optional.empty();
        } else if (atLeastTwoOf(typeRedirect.isPresent(), interOwnerContainer.isPresent(), intraOwnerContainer.isPresent())) {
            // If the inner class has more than one of @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer.
            innerClassExceptions.addException(new MoreThanOneContainerException(Type.getObjectType(innerClassNode.name)));
            // We don't know what the src/dst owners are, we can't continue parsing this inner class.
            return Optional.empty();
        }

        Type[] srcType = new Type[1]; // java is dumb
        Type[] dstType = new Type[1];
        boolean[] nonStaticRedirectsAllowed = new boolean[1];

        typeRedirect.ifPresent(redirect -> {
            srcType[0] = redirect.srcType();
            dstType[0] = redirect.dstType();
            nonStaticRedirectsAllowed[0] = true;
            data.typeRedirects.add(redirect);
        });

        interOwnerContainer.ifPresent(container -> {
            srcType[0] = container.srcType();
            dstType[0] = container.dstType();
            nonStaticRedirectsAllowed[0] = false;
        });

        intraOwnerContainer.ifPresent(container -> {
            srcType[0] = container.type();
            dstType[0] = container.type();
            nonStaticRedirectsAllowed[0] = true;
        });

        if (!nonStaticRedirectsAllowed[0]) {
            // Verify that there are no non-static members
            boolean nonStaticMembersExist = innerClassNode.methods.stream()
                    // filter the default constructor, it's not a valid redirect anyway.
                    .filter(methodNode -> !((methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) && methodNode.desc.equals("()V")))
                    .anyMatch(method -> (method.access & ACC_STATIC) == 0) |
                    innerClassNode.fields.stream().anyMatch(field -> (field.access & ACC_STATIC) == 0);
            if (nonStaticMembersExist) {
                innerClassExceptions.addException(new InterOwnerContainerHasNonStaticRedirects(Type.getObjectType(innerClassNode.name)));
                // The layout is illegal, we can't continue parsing this inner class.
                return Optional.empty();
            }
        }

        data.srcType = srcType[0];
        data.dstType = dstType[0];
        data.superDataName = innerClassNode.superName;

        parseFields(innerClassNode, srcType[0], dstType[0],
                data.fieldRedirects,
                data.fieldToMethodRedirects,
                innerClassExceptions
        );

        parseMethods(innerClassNode, srcType[0], dstType[0],
                data.methodRedirects,
                data.fieldToMethodRedirects,
                data.constructorToFactoryRedirects,
                innerClassExceptions
        );

        return Optional.of(data);
    }

    private static void parseFields(ClassNode innerClassNode, Type srcType, Type dstType, Set<FieldRedirectImpl> fieldRedirects,
                                    Set<FieldToMethodRedirectImpl> fieldToMethodRedirects, DasmClassExceptions exceptions) {
        for (FieldNode fieldNode : innerClassNode.fields) {
            DasmFieldExceptions fieldExceptions = exceptions.addNested(new DasmFieldExceptions(fieldNode));
            try {
                Optional<FieldRedirectImpl> fieldRedirect = FieldRedirectImpl.parseFieldRedirect(srcType, fieldNode, dstType);
                if (fieldRedirect.isPresent()) {
                    fieldRedirects.add(fieldRedirect.get());
                } else {
                    fieldExceptions.addException(new FieldMissingFieldRedirectAnnotationException(fieldNode));
                }
            } catch (RefImpl.RefAnnotationGivenNoArguments | FieldRedirectImpl.FieldRedirectHasEmptySrcName e) {
                fieldExceptions.addException(e);
            }
        }
    }

    private static void parseMethods(ClassNode innerClassNode, Type srcType, Type dstType,
                                     Set<MethodRedirectImpl> methodRedirects, Set<FieldToMethodRedirectImpl> fieldToMethodRedirects,
                                     Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects, DasmClassExceptions exceptions) {
        for (MethodNode methodNode : innerClassNode.methods) {
            if ((methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) && (methodNode.signature == null || methodNode.signature.equals("()V"))) {
                continue; // Skip default empty constructor
            }

            DasmMethodExceptions methodExceptions = exceptions.addNested(new DasmMethodExceptions(methodNode));

            Optional<MethodRedirectImpl> methodRedirect = Optional.empty();
            Optional<FieldToMethodRedirectImpl> fieldToMethodRedirect = Optional.empty();
            Optional<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirect = Optional.empty();
            try {
                methodRedirect = MethodRedirectImpl.parseMethodRedirect(
                        srcType,
                        (innerClassNode.access & ACC_INTERFACE) != 0,
                        methodNode,
                        dstType
                );
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.InvalidMethodSignature |
                     MethodSigImpl.EmptySrcName e) {
                methodExceptions.addException(e);
            }
            try {
                fieldToMethodRedirect = FieldToMethodRedirectImpl.parse(
                        srcType,
                        (innerClassNode.access & ACC_INTERFACE) != 0,
                        methodNode,
                        dstType
                );
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.InvalidMethodSignature |
                     MethodSigImpl.EmptySrcName e) {
                methodExceptions.addException(e);
            }
            try {
                constructorToFactoryRedirect = ConstructorToFactoryRedirectImpl.parse(
                        srcType,
                        (innerClassNode.access & ACC_INTERFACE) != 0,
                        methodNode,
                        dstType
                );
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.InvalidMethodSignature |
                     MethodSigImpl.EmptySrcName e) {
                methodExceptions.addException(e);
            }

            if (atLeastTwoOf(methodRedirect.isPresent(), fieldToMethodRedirect.isPresent(), constructorToFactoryRedirect.isPresent())) {
                // if both are present, add exception and return
                methodExceptions.addException(new MoreThanOneMethodRedirect(methodNode));
                return;
            } else if (!methodRedirect.isPresent() && !fieldToMethodRedirect.isPresent() && !constructorToFactoryRedirect.isPresent()) {
                // if none are present, add exception and return
                methodExceptions.addException(new MissingMethodRedirectRedirect(methodNode));
                return;
            }

            // only one must be present by this point
            methodRedirect.ifPresent(methodRedirects::add);
            fieldToMethodRedirect.ifPresent(fieldToMethodRedirects::add);
            constructorToFactoryRedirect.ifPresent(constructorToFactoryRedirects::add);
        }
    }

    private static class OwnerChanger implements MappingsProvider {
        private final Map<String, String> typeMapping = new HashMap<>();

        OwnerChanger(String oldSrc, String newSrc, String oldDst, String newDst) {
            this.typeMapping.put(oldSrc, newSrc);
            this.typeMapping.put(oldDst, newDst);
        }

        @Override
        public String mapFieldName(String owner, String fieldName, String descriptor) {
            return fieldName;
        }

        @Override
        public String mapMethodName(String owner, String methodName, String descriptor) {
            return methodName;
        }

        @Override
        public String mapClassName(String className) {
            return typeMapping.getOrDefault(className, className);
        }

        private ClassField remap(ClassField classField) {
            return new ClassField(this.remapType(classField.owner()), this.remapType(classField.mappingsOwner()), classField.type(), classField.name());
        }

        private ClassMethod remap(ClassMethod classMethod) {
            return new ClassMethod(this.remapType(classMethod.owner()), this.remapType(classMethod.mappingsOwner()), classMethod.method());
        }

        public FieldToMethodRedirectImpl remap(FieldToMethodRedirectImpl redirect) {
            return new FieldToMethodRedirectImpl(
                    this.remap(redirect.srcField()),
                    this.remap(redirect.getterDstMethod()),
                    redirect.setterDstMethod().map(this::remap),
                    redirect.isStatic(),
                    redirect.isDstOwnerInterface()
            );
        }

        public ConstructorToFactoryRedirectImpl remap(ConstructorToFactoryRedirectImpl redirect) {
            return new ConstructorToFactoryRedirectImpl(
                    this.remap(redirect.srcConstructor()),
                    this.remapType(redirect.dstOwner()),
                    redirect.dstName(),
                    redirect.isDstOwnerInterface()
            );
        }

        public FieldRedirectImpl remap(FieldRedirectImpl redirect) {
            return new FieldRedirectImpl(
                    this.remap(redirect.srcField()),
                    this.remapType(redirect.dstOwner()),
                    redirect.dstName()
            );
        }

        public MethodRedirectImpl remap(MethodRedirectImpl redirect) {
            return new MethodRedirectImpl(
                    this.remap(redirect.srcMethod()),
                    this.remapType(redirect.dstOwner()),
                    redirect.dstName(),
                    redirect.isStatic(),
                    redirect.isDstOwnerInterface()
            );
        }

        public TypeRedirectImpl remap(TypeRedirectImpl redirect) {
            return new TypeRedirectImpl(
                    this.remapType(redirect.srcType()),
                    this.remapType(redirect.dstType()),
                    redirect.isDstInterface()
            );
        }
    }

    public static class InterOwnerContainerHasNonStaticRedirects extends DasmException {
        public InterOwnerContainerHasNonStaticRedirects(Type type) {
            super("InterOwnerContainer " + simpleClassNameOf(type) + " contains non-static redirects which is invalid." +
                    "Consider using @TypeRedirect instead.");
        }
    }

    public static class MoreThanOneContainerException extends DasmException {
        public MoreThanOneContainerException(Type type) {
            super(simpleClassNameOf(type) + " has more than one of  @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer");
        }
    }

    public static class MissingContainerException extends DasmException {
        public MissingContainerException(Type redirectSetType) {
            super(simpleClassNameOf(redirectSetType) + " is missing one of @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer.");
        }
    }

    public static class MissingMethodRedirectRedirect extends DasmAnnotationException {
        public MissingMethodRedirectRedirect(MethodNode methodNode) {
            super("Method `" + methodNode.name + "` is missing a @MethodRedirect, @FieldToMethodRedirect or a @ConstructorToFactoryRedirect annotation.");
        }
    }

    public static class MoreThanOneMethodRedirect extends DasmAnnotationException {
        public MoreThanOneMethodRedirect(MethodNode methodNode) {
            super("Method `" + methodNode.name + "` has more than one of @MethodRedirect, @FieldToMethodRedirect and @ConstructorToFactoryRedirect annotations.");
        }
    }

    public static class MissingRedirectSetAnnotationException extends DasmException {
        public MissingRedirectSetAnnotationException(Type redirectSetType) {
            super(simpleClassNameOf(redirectSetType) + " is missing @RedirectSet annotation");
        }
    }

    public static class NonInterfaceIsUsedAsRedirectSetException extends DasmException {
        public NonInterfaceIsUsedAsRedirectSetException(Type redirectSetType) {
            super("Non-interface " + simpleClassNameOf(redirectSetType) + " is used as a redirect set");
        }
    }
}
