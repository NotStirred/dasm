package io.github.notstirred.dasm.annotation;

import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.annotation.parse.TransformFromMethodImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.transform.ApplicationStage;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.transformer.ClassTransform;
import io.github.notstirred.dasm.transformer.MethodTransform;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.Either;
import io.github.notstirred.dasm.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationIfPresent;
import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationValues;

public class AnnotationParser {
    private final ClassNodeProvider provider;
    private final Map<Type, RedirectSetImpl> redirectSetsByType = new HashMap<>();

    public AnnotationParser(ClassNodeProvider provider) {
        this.provider = provider;
    }

    @RequiredArgsConstructor
    public static class FindRedirectsException extends DasmAnnotationException {
        public final String message;
        public final ClassNode classNode;
    }


    @RequiredArgsConstructor
    public static class BuildClassTargetException extends DasmAnnotationException {
        public final String message;
        public final ClassNode classNode;
    }

    public void findRedirectSets(ClassNode targetClass) throws FindRedirectsException {
        FindRedirectsException suppressedExceptions = new FindRedirectsException("", targetClass);

        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, Dasm.class, "value", suppressedExceptions);
        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, TransformFromClass.class, "sets", suppressedExceptions);

        for (MethodNode methodNode : targetClass.methods) {
            findRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddTransformToSets.class, "value", suppressedExceptions);
            findRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddFieldToSets.class, "sets", suppressedExceptions);
            findRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddMethodToSets.class, "sets", suppressedExceptions);
        }

        if (suppressedExceptions.getSuppressed().length > 0) {
            throw suppressedExceptions;
        }
    }

    public Optional<Either<ClassTransform, Collection<MethodTransform>>> buildClassTarget(ClassNode targetClass, String methodPrefix)
            throws BuildClassTargetException {
        Type targetType = Type.getType(TypeUtil.classNameToDescriptor(targetClass.name));

        BuildClassTargetException suppressedExceptions = new BuildClassTargetException("", targetClass);

        AnnotationNode transformFromClassNode = getAnnotationIfPresent(targetClass.invisibleAnnotations, TransformFromClass.class);
        if (transformFromClassNode != null) {
            Map<String, Object> values = getAnnotationValues(transformFromClassNode, TransformFromClass.class);

            try {
                Type srcType = RefImpl.parseRefAnnotation((AnnotationNode) values.get("value"));
                ApplicationStage stage = (ApplicationStage) values.get("stage");

                // FIXME: this should verify that there are no method transforms inside this class,
                return Optional.of(Either.left(
                        new ClassTransform(srcType, targetType, new ArrayList<>(), stage)
                ));
            } catch (RefImpl.RefAnnotationGivenInvalidArguments e) {
                suppressedExceptions.addSuppressed(e);
            }
        }

        AnnotationNode dasmNode = getAnnotationIfPresent(targetClass.invisibleAnnotations, Dasm.class);
        if (dasmNode != null) {
            Map<String, Object> values = getAnnotationValues(dasmNode, Dasm.class);
            @SuppressWarnings("unchecked")
            List<RedirectSetImpl> defaultRedirectSets = ((List<Type>) values.get("value")).stream()
                    .map(this.redirectSetsByType::get).collect(Collectors.toList());

            List<MethodTransform> methodTransforms = new ArrayList<>();
            for (Iterator<MethodNode> iterator = targetClass.methods.iterator(); iterator.hasNext(); ) {
                MethodNode method = iterator.next();
                AnnotationNode transformFromMethodAnnotation = getAnnotationIfPresent(method.invisibleAnnotations, TransformFromMethod.class);
                if (transformFromMethodAnnotation == null) {
                    continue;
                }
                iterator.remove();

                TransformFromMethodImpl transformFromMethod;
                try {
                    transformFromMethod = TransformFromMethodImpl.parse(transformFromMethodAnnotation);
                } catch (MethodSigImpl.InvalidMethodSignature | RefImpl.RefAnnotationGivenInvalidArguments e) {
                    suppressedExceptions.addSuppressed(e);
                    continue;
                }

                Type methodOwner = transformFromMethod.copyFrom().orElse(targetType);

                List<RedirectSetImpl> redirectSets = transformFromMethod.overriddenRedirectSets().map(types ->
                                types.stream().map(this.redirectSetsByType::get).collect(Collectors.toList()))
                        .orElse(defaultRedirectSets);

                // FIXME: figure out if there is a way to avoid this with mixin.
                // Name is modified here to prevent mixin from overwriting it. We remove this prefix in postApply.
                String prefixedMethodName = methodPrefix + method.name
                        .replace("<init>", "__init__")
                        .replace("<clinit>", "__clinit__");

                methodTransforms.add(new MethodTransform(
                        new ClassMethod(methodOwner, methodOwner, transformFromMethod.srcMethod()),
                        prefixedMethodName,
                        redirectSets,
                        transformFromMethod.stage()
                ));
            }
            return Optional.of(Either.right(methodTransforms));
        }

        if (suppressedExceptions.getSuppressed().length > 0) {
            throw suppressedExceptions;
        }

        return Optional.empty();
    }

    private void findRedirectSetsForAnnotation(List<AnnotationNode> annotations, Class<?> annotationClass, String setsAnnotationField,
                                               DasmAnnotationException suppressedExceptions) {
        AnnotationNode annotationNode = getAnnotationIfPresent(annotations, annotationClass);
        if (annotationNode != null) {
            Map<String, Object> values = getAnnotationValues(annotationNode, annotationClass);
            @SuppressWarnings("unchecked") List<Type> sets = (List<Type>) values.get(setsAnnotationField);
            for (Type redirectSetType : sets) {
                try {
                    findRedirectSetsForType(redirectSetType);
                } catch (NoSuchTypeExists | RedirectSetImpl.RedirectSetParseException e) {
                    suppressedExceptions.addSuppressed(e);
                }
            }
        }
    }

    private void findRedirectSetsForType(Type redirectSetType)
            throws NoSuchTypeExists, RedirectSetImpl.RedirectSetParseException {
        RedirectSetImpl existingSet = this.redirectSetsByType.get(redirectSetType);
        if (existingSet == null) {
            ClassNode redirectSetClass = this.provider.classNode(redirectSetType);
            existingSet = RedirectSetImpl.parse(redirectSetClass, this.provider);
            this.redirectSetsByType.put(redirectSetType, existingSet);
        }

        for (Type superRedirectSet : existingSet.superRedirectSets()) {
            findRedirectSetsForType(superRedirectSet);
        }
    }
}
