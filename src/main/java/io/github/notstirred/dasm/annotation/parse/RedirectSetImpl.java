package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.parse.redirects.*;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl.FieldMissingFieldRedirectAnnotationException;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmFieldExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmMethodExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import lombok.Getter;
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

    public static RedirectSetImpl parse(ClassNode redirectSetClassNode, ClassNodeProvider provider) throws DasmWrappedExceptions {
        List<Type> superRedirectSets = new ArrayList<>();

        Set<FieldToMethodRedirectImpl> fieldToMethodRedirects = new HashSet<>();
        Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects = new HashSet<>();
        Set<FieldRedirectImpl> fieldRedirects = new HashSet<>();
        Set<MethodRedirectImpl> methodRedirects = new HashSet<>();
        Set<TypeRedirectImpl> typeRedirects = new HashSet<>();

        DasmClassExceptions redirectSetExceptions = new DasmClassExceptions("An exception occurred when parsing redirect set", redirectSetClassNode);

        AnnotationNode annotationNode = getAnnotationIfPresent(redirectSetClassNode.invisibleAnnotations, RedirectSet.class);
        if (annotationNode == null) {
            redirectSetExceptions.addSuppressed(new MissingRedirectSetAnnotationException(Type.getObjectType(redirectSetClassNode.name)));
        }

        if ((redirectSetClassNode.access & ACC_INTERFACE) == 0) {
            redirectSetExceptions.addSuppressed(new NonInterfaceIsUsedAsRedirectSetException(Type.getObjectType(redirectSetClassNode.name)));
        }

        // Add inherited redirect sets
        for (String itf : redirectSetClassNode.interfaces) {
            superRedirectSets.add(Type.getObjectType(itf));
        }

        // Discover type/field/method redirects in innerClass
        for (InnerClassNode innerClass : redirectSetClassNode.innerClasses) {
            if (innerClass.name.equals(redirectSetClassNode.name)) {
                continue; // `innerClasses` seems to contain the outer class too.
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

            Optional<TypeRedirectImpl> typeRedirect = TypeRedirectImpl.parse(innerClassNode, innerClassExceptions);
            Optional<InterOwnerContainerImpl> interOwnerContainer = InterOwnerContainerImpl.parse(innerClassNode, innerClassExceptions);
            Optional<IntraOwnerContainerImpl> intraOwnerContainer = IntraOwnerContainerImpl.parse(innerClassNode, innerClassExceptions);

            if (innerClassExceptions.hasWrapped()) {
                continue; // If creating the typeRedirect/interOwnerContainer has errored, we should exit now otherwise more errors caused by this will show from below
            }

            if (!(typeRedirect.isPresent() | interOwnerContainer.isPresent() | intraOwnerContainer.isPresent())) {
                // The inner class must have one of @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer, but does not.
                innerClassExceptions.addException(new MissingContainerException(Type.getObjectType(innerClassNode.name)));
                // We don't know what the src/dst owners are, we can't continue parsing this inner class.
                continue;
            } else if (atLeastTwoOf(typeRedirect.isPresent(), interOwnerContainer.isPresent(), intraOwnerContainer.isPresent())) {
                // If the inner class has more than one of @TypeRedirect, @InterOwnerContainer, @IntraOwnerContainer.
                innerClassExceptions.addException(new MoreThanOneContainerException(Type.getObjectType(innerClassNode.name)));
                // We don't know what the src/dst owners are, we can't continue parsing this inner class.
                continue;
            }

            Type[] srcType = new Type[1]; // java is dumb
            Type[] dstType = new Type[1];
            boolean[] nonStaticRedirectsAllowed = new boolean[1];

            typeRedirect.ifPresent(redirect -> {
                srcType[0] = redirect.srcType();
                dstType[0] = redirect.dstType();
                nonStaticRedirectsAllowed[0] = true;
                typeRedirects.add(redirect);
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
                        .filter(methodNode -> !(methodNode.name.equals("<init>") && methodNode.desc.equals("()V")))
                        .anyMatch(method -> (method.access & ACC_STATIC) == 0) |
                        innerClassNode.fields.stream().anyMatch(field -> (field.access & ACC_STATIC) == 0);
                if (nonStaticMembersExist) {
                    innerClassExceptions.addException(new InterOwnerContainerHasNonStaticRedirects(Type.getObjectType(innerClassNode.name)));
                    // The layout is illegal, we can't continue parsing this inner class.
                    continue;
                }
            }

            parseFields(innerClassNode, srcType[0], dstType[0], fieldRedirects, fieldToMethodRedirects, innerClassExceptions);

            parseMethods(innerClassNode, srcType[0], dstType[0], methodRedirects, fieldToMethodRedirects, constructorToFactoryRedirects, innerClassExceptions);
        }

        redirectSetExceptions.throwIfHasWrapped();

        return new RedirectSetImpl(superRedirectSets, fieldToMethodRedirects, constructorToFactoryRedirects, fieldRedirects, methodRedirects, typeRedirects);
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
            if (methodNode.name.equals("<init>") && (methodNode.signature == null || methodNode.signature.equals("()V"))) {
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
