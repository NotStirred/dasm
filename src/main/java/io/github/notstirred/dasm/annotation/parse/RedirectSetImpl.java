package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.redirects.*;
import io.github.notstirred.dasm.redirects.FieldRedirectImpl.FieldMissingFieldRedirectAnnotationException;
import io.github.notstirred.dasm.redirects.MethodRedirectImpl.MethodMissingMethodRedirectAnnotationException;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.*;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationIfPresent;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

public class RedirectSetImpl {
    public final List<Type> superRedirectSets;

    public final Set<FieldToMethodRedirectImpl> fieldToMethodRedirects;
    public final Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects;
    public final Set<FieldRedirectImpl> fieldRedirects;
    public final Set<MethodRedirectImpl> methodRedirects;
    public final Set<TypeRedirectImpl> typeRedirects;

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

    public static RedirectSetImpl parse(ClassNode redirectSetClassNode, ClassNodeProvider provider) throws RedirectSetParseException {
        List<Type> superRedirectSets = new ArrayList<>();

        Set<FieldToMethodRedirectImpl> fieldToMethodRedirects = new HashSet<>();
        Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects = new HashSet<>();
        Set<FieldRedirectImpl> fieldRedirects = new HashSet<>();
        Set<MethodRedirectImpl> methodRedirects = new HashSet<>();
        Set<TypeRedirectImpl> typeRedirects = new HashSet<>();

        RedirectSetParseException suppressedExceptions = new RedirectSetParseException();

        AnnotationNode annotationNode = getAnnotationIfPresent(redirectSetClassNode.invisibleAnnotations, RedirectSet.class);
        if (annotationNode == null) {
            suppressedExceptions.addSuppressed(new MissingRedirectSetAnnotationException(Type.getObjectType(redirectSetClassNode.name)));
        }

        if ((redirectSetClassNode.access & ACC_INTERFACE) == 0) {
            suppressedExceptions.addSuppressed(new NonInterfaceIsUsedAsRedirectSetException(Type.getObjectType(redirectSetClassNode.name)));
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
                suppressedExceptions.addSuppressed(new RedirectSetInnerExceptions(redirectSetClassNode, Collections.singletonList(e)));
                // The inner class doesn't exist, we can't begin parsing it.
                continue;
            }

            List<Exception> innerExceptions = new ArrayList<>();
            Optional<TypeRedirectImpl> typeRedirect = Optional.empty();
            try {
                typeRedirect = TypeRedirectImpl.parseTypeRedirect(innerClassNode);
            } catch (RefImpl.RefAnnotationGivenInvalidArguments e) {
                innerExceptions.add(e);
            }
            Optional<RedirectContainerImpl> redirectContainer = Optional.empty();
            try {
                redirectContainer = RedirectContainerImpl.parseRedirectContainer(innerClassNode);
            } catch (RefImpl.RefAnnotationGivenInvalidArguments e) {
                innerExceptions.add(e);
            }
            if (!(typeRedirect.isPresent() | redirectContainer.isPresent())) {
                // If the inner class has neither @TypeRedirect nor @RedirectContainer, report it.
                innerExceptions.add(new MissingTypeRedirectAndRedirectContainerException(Type.getObjectType(innerClassNode.name)));
                // We don't know what the src/dst owners are, we can't continue parsing this inner class.
                continue;
            } else if (typeRedirect.isPresent() & redirectContainer.isPresent()) {
                // If the inner class has both @TypeRedirect and @RedirectContainer, report it.
                innerExceptions.add(new BothTypeRedirectAndRedirectContainerException(Type.getObjectType(innerClassNode.name)));
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

            parseFields(innerClassNode, srcType[0], dstType[0], fieldRedirects, innerExceptions);

            parseMethods(innerClassNode, srcType[0], dstType[0], methodRedirects, innerExceptions);

            if (!innerExceptions.isEmpty()) {
                suppressedExceptions.addSuppressed(new RedirectSetInnerExceptions(innerClassNode, innerExceptions));
            }
        }

        if (suppressedExceptions.getSuppressed().length > 0) {
            throw suppressedExceptions;
        }
        return new RedirectSetImpl(superRedirectSets, fieldToMethodRedirects, constructorToFactoryRedirects, fieldRedirects, methodRedirects, typeRedirects);
    }

    private static void parseFields(ClassNode innerClassNode, Type srcType, Type dstType, Set<FieldRedirectImpl> fieldRedirects,
                                    List<Exception> exceptions) {
        for (FieldNode fieldNode : innerClassNode.fields) {
            try {
                Optional<FieldRedirectImpl> fieldRedirect = FieldRedirectImpl.parseFieldRedirect(srcType, fieldNode, dstType);
                if (fieldRedirect.isPresent()) {
                    fieldRedirects.add(fieldRedirect.get());
                } else {
                    exceptions.add(new FieldMissingFieldRedirectAnnotationException(fieldNode));
                }
            } catch (RefImpl.RefAnnotationGivenInvalidArguments | FieldRedirectImpl.FieldRedirectHasEmptySrcName e) {
                exceptions.add(e);
            }
        }
    }

    private static void parseMethods(ClassNode innerClassNode, Type srcType, Type dstType, Set<MethodRedirectImpl> methodRedirects,
                                     List<Exception> exceptions) {
        for (MethodNode methodNode : innerClassNode.methods) {
            if (methodNode.name.equals("<init>") && (methodNode.signature == null || methodNode.signature.equals("()V"))) {
                continue; // Skip default empty constructor
            }

            Optional<MethodRedirectImpl> methodRedirect;
            try {
                methodRedirect = MethodRedirectImpl.parseMethodRedirect(srcType, methodNode, dstType);
                if (methodRedirect.isPresent()) {
                    methodRedirects.add(methodRedirect.get());
                } else {
                    exceptions.add(new MethodMissingMethodRedirectAnnotationException(methodNode));
                }
            } catch (RefImpl.RefAnnotationGivenInvalidArguments | MethodRedirectImpl.MethodRedirectHasEmptySrcName e) {
                exceptions.add(e);
            }
        }
    }

    public static class RedirectSetParseException extends DasmAnnotationException { }

    /**
     * Contains the suppressed exceptions while parsing a redirect set.
     */
    public static class RedirectSetInnerExceptions extends DasmAnnotationException {
        public final ClassNode classNode;
        public final List<Exception> causes;

        public RedirectSetInnerExceptions(ClassNode classNode, List<Exception> causes) {
            this.classNode = classNode;
            this.causes = Collections.unmodifiableList(causes);
        }
    }

    @RequiredArgsConstructor
    public static class BothTypeRedirectAndRedirectContainerException extends Exception {
        public final Type type;
    }

    @RequiredArgsConstructor
    public static class MissingTypeRedirectAndRedirectContainerException extends Exception {
        public final Type type;
    }

    @RequiredArgsConstructor
    public static class MissingRedirectSetAnnotationException extends Exception {
        public final Type redirectSetType;
    }

    @RequiredArgsConstructor
    public static class NonInterfaceIsUsedAsRedirectSetException extends Exception {
        public final Type redirectSetType;
    }
}
