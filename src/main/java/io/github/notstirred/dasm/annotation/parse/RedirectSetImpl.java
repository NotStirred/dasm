package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.parse.redirects.*;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl.FieldMissingFieldRedirectAnnotationException;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl.MethodMissingMethodRedirectAnnotationException;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
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
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

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
        this.superRedirectSets = Collections.unmodifiableList(superRedirectSets);
        this.fieldToMethodRedirects = Collections.unmodifiableSet(fieldToMethodRedirects);
        this.constructorToFactoryRedirects = Collections.unmodifiableSet(constructorToFactoryRedirects);
        this.fieldRedirects = Collections.unmodifiableSet(fieldRedirects);
        this.methodRedirects = Collections.unmodifiableSet(methodRedirects);
        this.typeRedirects = Collections.unmodifiableSet(typeRedirects);
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

            Optional<TypeRedirectImpl> typeRedirect = TypeRedirectImpl.parseTypeRedirect(innerClassNode, innerClassExceptions);
            Optional<RedirectContainerImpl> redirectContainer = RedirectContainerImpl.parseRedirectContainer(innerClassNode, innerClassExceptions);

            if (innerClassExceptions.hasWrapped()) {
                continue; // If creating the typeRedirect/redirectContainer has errored, we should exit now otherwise more errors caused by this will show from below
            }

            if (!(typeRedirect.isPresent() | redirectContainer.isPresent())) {
                // If the inner class has neither @TypeRedirect nor @RedirectContainer, report it.
                innerClassExceptions.addException(new MissingTypeRedirectAndRedirectContainerException(Type.getObjectType(innerClassNode.name)));
                // We don't know what the src/dst owners are, we can't continue parsing this inner class.
                continue;
            } else if (typeRedirect.isPresent() & redirectContainer.isPresent()) {
                // If the inner class has both @TypeRedirect and @RedirectContainer, report it.
                innerClassExceptions.addException(new BothTypeRedirectAndRedirectContainerException(Type.getObjectType(innerClassNode.name)));
                // We don't know what the src/dst owners are, we can't continue parsing this inner class.
                continue;
            }

            Type[] srcType = new Type[1]; // java is dumb
            Type[] dstType = new Type[1];

            typeRedirect.ifPresent(redirect -> {
                srcType[0] = redirect.srcType();
                dstType[0] = redirect.dstType();
                typeRedirects.add(redirect);
            });

            redirectContainer.ifPresent(container -> {
                srcType[0] = container.type();
                dstType[0] = container.type();
            });

            parseFields(innerClassNode, srcType[0], dstType[0], fieldRedirects, innerClassExceptions);

            parseMethods(innerClassNode, srcType[0], dstType[0], methodRedirects, innerClassExceptions);
        }

        redirectSetExceptions.throwIfHasWrapped();

        return new RedirectSetImpl(superRedirectSets, fieldToMethodRedirects, constructorToFactoryRedirects, fieldRedirects, methodRedirects, typeRedirects);
    }

    private static void parseFields(ClassNode innerClassNode, Type srcType, Type dstType, Set<FieldRedirectImpl> fieldRedirects,
                                    DasmClassExceptions exceptions) {
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

    private static void parseMethods(ClassNode innerClassNode, Type srcType, Type dstType, Set<MethodRedirectImpl> methodRedirects,
                                     DasmClassExceptions exceptions) {
        for (MethodNode methodNode : innerClassNode.methods) {
            if (methodNode.name.equals("<init>") && (methodNode.signature == null || methodNode.signature.equals("()V"))) {
                continue; // Skip default empty constructor
            }

            DasmMethodExceptions methodExceptions = exceptions.addNested(new DasmMethodExceptions(methodNode));

            Optional<MethodRedirectImpl> methodRedirect;
            try {
                methodRedirect = MethodRedirectImpl.parseMethodRedirect(
                        srcType,
                        (innerClassNode.access & ACC_INTERFACE) != 0,
                        methodNode,
                        dstType
                );
                if (methodRedirect.isPresent()) {
                    methodRedirects.add(methodRedirect.get());
                } else {
                    methodExceptions.addException(new MethodMissingMethodRedirectAnnotationException(methodNode));
                }
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodRedirectImpl.MethodRedirectHasEmptySrcName | MethodSigImpl.InvalidMethodSignature |
                     MethodSigImpl.EmptySrcName e) {
                methodExceptions.addException(e);
            }
        }
    }

    public static class BothTypeRedirectAndRedirectContainerException extends DasmException {
        public BothTypeRedirectAndRedirectContainerException(Type type) {
            super(type.getClassName() + " has both @TypeRedirect and @Redirect container");
        }
    }

    public static class MissingTypeRedirectAndRedirectContainerException extends DasmException {
        public MissingTypeRedirectAndRedirectContainerException(Type redirectSetType) {
            super(redirectSetType.getClassName() + " is missing one of @TypeRedirect or @RedirectContainer annotations");
        }
    }

    public static class MissingRedirectSetAnnotationException extends DasmException {

        public MissingRedirectSetAnnotationException(Type redirectSetType) {
            super(redirectSetType.getClassName() + " is missing @RedirectSet annotation");
        }
    }

    public static class NonInterfaceIsUsedAsRedirectSetException extends DasmException {
        public NonInterfaceIsUsedAsRedirectSetException(Type redirectSetType) {
            super("Non-interface " + redirectSetType.getClassName() + " is used as a redirect set");
        }
    }
}
