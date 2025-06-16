package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.parse.redirects.*;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl.FieldMissingFieldRedirectAnnotationException;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.notify.Notification;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.NotifyStack;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationIfPresent;
import static io.github.notstirred.dasm.util.Util.atLeastTwoOf;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

@Data
public class RedirectSetImpl {
    private final List<Type> superRedirectSets;

    private final List<Container> containers;

    @Getter
    public static class Container {
        private Type type;
        private Type srcType;
        private Type dstType;

        private @Nullable Container superContainer;
        private Kind kind;

        private final Set<FieldToMethodRedirectImpl> fieldToMethodRedirects = new HashSet<>();
        private final Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects = new HashSet<>();
        private final Set<FieldRedirectImpl> fieldRedirects = new HashSet<>();
        private final Set<MethodRedirectImpl> methodRedirects = new HashSet<>();

        private final Set<TypeRedirectImpl> typeRedirects = new HashSet<>();
    }

    public enum Kind {
        TYPE_REDIRECT,
        INTER_OWNER_CONTAINER,
        INTRA_OWNER_CONTAINER
    }

    public static Optional<RedirectSetImpl> parse(ClassNode redirectSetClassNode, ClassNodeProvider provider, NotifyStack exceptions) {
        List<Type> superRedirectSets = new ArrayList<>();

        NotifyStack redirectSetExceptions = exceptions.push(redirectSetClassNode);

        AnnotationNode annotationNode = getAnnotationIfPresent(redirectSetClassNode.invisibleAnnotations, RedirectSet.class);
        if (annotationNode == null) {
            redirectSetExceptions.notify(new MissingRedirectSetAnnotationException(Type.getObjectType(redirectSetClassNode.name)));
        }

        if ((redirectSetClassNode.access & ACC_INTERFACE) == 0) {
            redirectSetExceptions.notify(new NonInterfaceIsUsedAsRedirectSetException(Type.getObjectType(redirectSetClassNode.name)));
        }

        // Add inherited redirect sets
        for (String itf : redirectSetClassNode.interfaces) {
            superRedirectSets.add(Type.getObjectType(itf));
        }

        Map<Type, InnerClassInfo> innerClassData = new HashMap<>();

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
                redirectSetExceptions.notifyFromException(e);
                // The inner class doesn't exist, we can't begin parsing it.
                continue;
            }
            NotifyStack innerClassExceptions = redirectSetExceptions.push(innerClassNode);
            parseInnerClass(innerClassNode, innerClassExceptions).ifPresent(superNameContainerPair ->
                    innerClassData.put(superNameContainerPair.container().type(), superNameContainerPair)
            );
        }

        innerClassData.values().forEach(innerClassInfo -> {
            NotifyStack innerClassExceptions = redirectSetExceptions.push(innerClassInfo.container.type);
            innerClassInfo.superNames.forEach(superName -> {
                Type superContainerType = Type.getObjectType(superName);
                if (superContainerType.equals(Type.getType(Object.class))) { // having no super type is always OK
                    return;
                }
                InnerClassInfo superContainerInfo = innerClassData.get(superContainerType);
                if (superContainerInfo == null) {
                    innerClassExceptions.notify(new SuperTypeInInvalidRedirectSet(innerClassInfo.container.type.getClassName(), superContainerType.getClassName()));
                    return;
                }
                if (innerClassInfo.container.superContainer != null) {
                    innerClassExceptions.notify(new MultipleContainerInheritanceNotImplemented(innerClassInfo.container));
                    return;
                }
                innerClassInfo.container.superContainer = superContainerInfo.container();
            });
        });

        return Optional.of(new RedirectSetImpl(superRedirectSets, innerClassData.values().stream().map(InnerClassInfo::container).collect(Collectors.toList())));
    }

    @Data
    private static class InnerClassInfo {
        private final Container container;
        private final List<String> superNames;
    }

    private static Optional<InnerClassInfo> parseInnerClass(ClassNode innerClassNode, NotifyStack innerClassExceptions) {
        Container data = new Container();

        Optional<TypeRedirectImpl> typeRedirect = TypeRedirectImpl.parse(innerClassNode, innerClassExceptions);
        Optional<InterOwnerContainerImpl> interOwnerContainer = InterOwnerContainerImpl.parse(innerClassNode, innerClassExceptions);
        Optional<IntraOwnerContainerImpl> intraOwnerContainer = IntraOwnerContainerImpl.parse(innerClassNode, innerClassExceptions);

        if (innerClassExceptions.hasError()) {
            return Optional.empty();
        }

        if (!(typeRedirect.isPresent() | interOwnerContainer.isPresent() | intraOwnerContainer.isPresent())) {
            // The inner class must have one of @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer, but does not.
            innerClassExceptions.notify(new MissingContainerException(Type.getObjectType(innerClassNode.name)));
            // We don't know what the src/dst owners are, we can't continue parsing this inner class.
            return Optional.empty();
        } else if (atLeastTwoOf(typeRedirect.isPresent(), interOwnerContainer.isPresent(), intraOwnerContainer.isPresent())) {
            // If the inner class has more than one of @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer.
            innerClassExceptions.notify(new MoreThanOneContainerException(Type.getObjectType(innerClassNode.name)));
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
            data.kind = Kind.TYPE_REDIRECT;
        });

        interOwnerContainer.ifPresent(container -> {
            srcType[0] = container.srcType();
            dstType[0] = container.dstType();
            nonStaticRedirectsAllowed[0] = false;
            data.kind = Kind.INTER_OWNER_CONTAINER;
        });

        intraOwnerContainer.ifPresent(container -> {
            srcType[0] = container.type();
            dstType[0] = container.type();
            nonStaticRedirectsAllowed[0] = true;
            data.kind = Kind.INTRA_OWNER_CONTAINER;
        });

        if (!nonStaticRedirectsAllowed[0]) {
            // Verify that there are no non-static members
            boolean nonStaticMembersExist = innerClassNode.methods.stream()
                    // filter the default constructor, it's not a valid redirect anyway.
                    .filter(methodNode -> !((methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) && methodNode.desc.equals("()V")))
                    .anyMatch(method -> (method.access & ACC_STATIC) == 0) |
                    innerClassNode.fields.stream().anyMatch(field -> (field.access & ACC_STATIC) == 0);
            if (nonStaticMembersExist) {
                innerClassExceptions.notify(new InterOwnerContainerHasNonStaticRedirects(Type.getObjectType(innerClassNode.name)));
                // The layout is illegal, we can't continue parsing this inner class.
                return Optional.empty();
            }
        }

        // Redirect set itself is valid beyond this point, so we always return the real object.
        // This allows set inheritance and other errors to work properly for any valid set even if its contents are invalid.
        // If the methods/fields error it will still be reported by the caller.

        data.type = Type.getObjectType(innerClassNode.name);
        data.srcType = srcType[0];
        data.dstType = dstType[0];

        parseFields(innerClassNode, data.srcType, data.dstType,
                data.fieldRedirects,
                data.fieldToMethodRedirects,
                innerClassExceptions
        );

        parseMethods(innerClassNode, data.srcType, data.dstType,
                data.methodRedirects,
                data.fieldToMethodRedirects,
                data.constructorToFactoryRedirects,
                innerClassExceptions
        );

        List<String> list = Stream.concat(innerClassNode.interfaces.stream(), Stream.of(innerClassNode.superName)).collect(Collectors.toList());
        return Optional.of(new InnerClassInfo(data, list));
    }

    private static void parseFields(ClassNode innerClassNode, Type srcType, Type dstType, Set<FieldRedirectImpl> fieldRedirects,
                                    Set<FieldToMethodRedirectImpl> fieldToMethodRedirects, NotifyStack exceptions) {
        for (FieldNode fieldNode : innerClassNode.fields) {
            NotifyStack fieldExceptions = exceptions.push(fieldNode);
            try {
                Optional<FieldRedirectImpl> fieldRedirect = FieldRedirectImpl.parseFieldRedirect(srcType, fieldNode, dstType);
                if (fieldRedirect.isPresent()) {
                    fieldRedirects.add(fieldRedirect.get());
                } else {
                    fieldExceptions.notify(new FieldMissingFieldRedirectAnnotationException(fieldNode));
                }
            } catch (RefImpl.RefAnnotationGivenNoArguments | FieldRedirectImpl.FieldRedirectHasEmptySrcName e) {
                fieldExceptions.notifyFromException(e);
            }
        }
    }

    private static void parseMethods(ClassNode innerClassNode, Type srcType, Type dstType,
                                     Set<MethodRedirectImpl> methodRedirects, Set<FieldToMethodRedirectImpl> fieldToMethodRedirects,
                                     Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects, NotifyStack exceptions) {
        for (MethodNode methodNode : innerClassNode.methods) {
            if ((methodNode.name.equals("<init>") || methodNode.name.equals("<clinit>")) && (methodNode.signature == null || methodNode.signature.equals("()V"))) {
                continue; // Skip default empty constructor
            }

            NotifyStack methodExceptions = exceptions.push(methodNode);

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
                methodExceptions.notifyFromException(e);
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
                methodExceptions.notifyFromException(e);
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
                methodExceptions.notifyFromException(e);
            }

            if (atLeastTwoOf(methodRedirect.isPresent(), fieldToMethodRedirect.isPresent(), constructorToFactoryRedirect.isPresent())) {
                // if both are present, add exception and return
                methodExceptions.notify(new MoreThanOneMethodRedirect(methodNode));
                return;
            } else if (!methodRedirect.isPresent() && !fieldToMethodRedirect.isPresent() && !constructorToFactoryRedirect.isPresent()) {
                // if none are present, add exception and return
                methodExceptions.notify(new MissingMethodRedirectRedirect(methodNode));
                return;
            }

            // only one must be present by this point
            methodRedirect.ifPresent(methodRedirects::add);
            fieldToMethodRedirect.ifPresent(fieldToMethodRedirects::add);
            constructorToFactoryRedirect.ifPresent(constructorToFactoryRedirects::add);
        }
    }

    public static class SuperTypeInInvalidRedirectSet extends Notification {
        public SuperTypeInInvalidRedirectSet(String containerName, String superContainerName) {
            super("`" + containerName + "` extends redirect `" + superContainerName + "` which is not within the same RedirectSet");
        }
    }

    public static class InterOwnerContainerHasNonStaticRedirects extends Notification {
        public InterOwnerContainerHasNonStaticRedirects(Type type) {
            super("InterOwnerContainer contains non-static redirects which is invalid." +
                    "Consider using @TypeRedirect instead.");
        }
    }

    public static class MoreThanOneContainerException extends Notification {
        public MoreThanOneContainerException(Type type) {
            super("Type has more than one of  @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer");
        }
    }

    public static class MissingContainerException extends Notification {
        public MissingContainerException(Type redirectSetType) {
            super("Type is missing one of @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer.");
        }
    }

    public static class MissingMethodRedirectRedirect extends Notification {
        public MissingMethodRedirectRedirect(MethodNode methodNode) {
            super("Method is missing a @MethodRedirect, @FieldToMethodRedirect or a @ConstructorToFactoryRedirect annotation.");
        }
    }

    public static class MoreThanOneMethodRedirect extends Notification {
        public MoreThanOneMethodRedirect(MethodNode methodNode) {
            super("Method has more than one of @MethodRedirect, @FieldToMethodRedirect and @ConstructorToFactoryRedirect annotations.");
        }
    }

    public static class MissingRedirectSetAnnotationException extends Notification {
        public MissingRedirectSetAnnotationException(Type redirectSetType) {
            super("Type is missing @RedirectSet annotation");
        }
    }

    public static class NonInterfaceIsUsedAsRedirectSetException extends Notification {
        public NonInterfaceIsUsedAsRedirectSetException(Type redirectSetType) {
            super("Non-interface is used as a redirect set");
        }
    }

    public static class MultipleContainerInheritanceNotImplemented extends Notification {
        public MultipleContainerInheritanceNotImplemented(Container container) {
            super("Multiple inheritance is not implemented yet. Occurs on: " + container.type.getClassName());
        }
    }
}
