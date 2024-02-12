package io.github.notstirred.dasm.annotation;

import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.annotation.parse.TransformFromMethodImpl;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddFieldToSetsImpl;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddMethodToSetsImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.TypeRedirectImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.transform.ApplicationStage;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromClass;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmFieldExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmMethodExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.transformer.ClassTransform;
import io.github.notstirred.dasm.transformer.MethodTransform;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.Either;
import io.github.notstirred.dasm.util.TypeUtil;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
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

    public void findRedirectSets(ClassNode targetClass) throws DasmWrappedExceptions {
        Type targetClassType = Type.getType(TypeUtil.classNameToDescriptor(targetClass.name));

        DasmClassExceptions classExceptions = new DasmClassExceptions("An exception occurred when finding used redirect sets in", targetClass);

        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, Dasm.class, "value", classExceptions);
        findRedirectSetsForAnnotation(targetClass.invisibleAnnotations, TransformFromClass.class, "sets", classExceptions);

        for (FieldNode fieldNode : targetClass.fields) {
            DasmFieldExceptions fieldExceptions = classExceptions.addNested(new DasmFieldExceptions(fieldNode));
            findRedirectSetsForAnnotation(fieldNode.invisibleAnnotations, AddFieldToSets.class, "sets", fieldExceptions);

            try {
                AddFieldToSetsImpl.parse(targetClassType, fieldNode).ifPresent(setsRedirectPair -> {
                    // All redirect sets for this method must already exist, so we can just use the map
                    setsRedirectPair.first().forEach(setType -> this.redirectSetsByType.get(setType).fieldRedirects().add(setsRedirectPair.second()));
                });
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.InvalidMethodSignature | MethodSigImpl.EmptySrcName e) {
                fieldExceptions.addException(e);
            }
        }

        for (MethodNode methodNode : targetClass.methods) {
            DasmMethodExceptions methodExceptions = classExceptions.addNested(new DasmMethodExceptions(methodNode));
            findRedirectSetsForAnnotation(methodNode.invisibleAnnotations, TransformFromMethod.class, "useRedirectSets", methodExceptions);
            findRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddTransformToSets.class, "value", methodExceptions);
            findRedirectSetsForAnnotation(methodNode.invisibleAnnotations, AddMethodToSets.class, "sets", methodExceptions);

            try {
                AddMethodToSetsImpl.parse(targetClassType, methodNode).ifPresent(setsRedirectPair -> {
                    // All redirect sets for this method must already exist, so we can just use the map
                    setsRedirectPair.first().forEach(setType -> this.redirectSetsByType.get(setType).methodRedirects().add(setsRedirectPair.second()));
                });
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.InvalidMethodSignature | MethodSigImpl.EmptySrcName e) {
                methodExceptions.addException(e);
            }
        }

        classExceptions.throwIfHasWrapped();
    }

    public Optional<Either<ClassTransform, Collection<MethodTransform>>> buildClassTarget(ClassNode targetClass, String methodPrefix)
            throws DasmWrappedExceptions {
        Type targetType = Type.getType(TypeUtil.classNameToDescriptor(targetClass.name));

        DasmClassExceptions classExceptions = new DasmClassExceptions("An exception occurred when looking for transforms in", targetClass);

        AnnotationNode transformFromClassNode = getAnnotationIfPresent(targetClass.invisibleAnnotations, TransformFromClass.class);
        if (transformFromClassNode != null) {
            Map<String, Object> values = getAnnotationValues(transformFromClassNode, TransformFromClass.class);

            try {
                Type srcType = RefImpl.parseRefAnnotation("value", values);
                ApplicationStage stage = (ApplicationStage) values.get("stage");

                AnnotationNode addToSetsAnnotation = getAnnotationIfPresent(targetClass.invisibleAnnotations, AddTransformToSets.class);
                if (addToSetsAnnotation != null) {
                    Map<String, Object> addToSets = getAnnotationValues(addToSetsAnnotation, AddTransformToSets.class);
                    List<Type> sets = (List<Type>) addToSets.get("value");

                    sets.forEach(set -> this.redirectSetsByType.get(set).typeRedirects().add(new TypeRedirectImpl(
                            srcType, targetType
                    )));
                }


                // FIXME: this should verify that there are no method transforms inside this class,
                return Optional.of(Either.left(
                        new ClassTransform(srcType, targetType, new ArrayList<>(), stage)
                ));
            } catch (RefImpl.RefAnnotationGivenNoArguments e) {
                classExceptions.addException(e);
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
                DasmMethodExceptions methodExceptions = classExceptions.addNested(new DasmMethodExceptions(method));

                TransformFromMethodImpl transformFromMethod;
                try {
                    transformFromMethod = TransformFromMethodImpl.parse(transformFromMethodAnnotation);
                } catch (MethodSigImpl.InvalidMethodSignature | RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.EmptySrcName e) {
                    methodExceptions.addException(e);
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

                MethodTransform transform = new MethodTransform(
                        new ClassMethod(methodOwner, methodOwner, transformFromMethod.srcMethod()),
                        prefixedMethodName,
                        redirectSets,
                        transformFromMethod.stage()
                );

                AnnotationNode addToSetsAnnotation = getAnnotationIfPresent(method.invisibleAnnotations, AddTransformToSets.class);
                if (addToSetsAnnotation != null) {
                    Map<String, Object> addToSets = getAnnotationValues(addToSetsAnnotation, AddTransformToSets.class);
                    List<Type> sets = (List<Type>) addToSets.get("value");
                    boolean isDstInterface = (boolean) addToSets.get("isDstInterface");

                    sets.forEach(set -> this.redirectSetsByType.get(set).methodRedirects().add(new MethodRedirectImpl(
                            transform.srcMethod(),
                            targetType,
                            transform.dstMethodName(),
                            isDstInterface
                    )));
                }

                methodTransforms.add(transform);
            }
            return Optional.of(Either.right(methodTransforms));
        }

        classExceptions.throwIfHasWrapped();

        return Optional.empty();
    }

    private void findRedirectSetsForAnnotation(List<AnnotationNode> annotations, Class<?> annotationClass, String setsAnnotationField,
                                               DasmWrappedExceptions exceptions) {
        AnnotationNode annotationNode = getAnnotationIfPresent(annotations, annotationClass);
        if (annotationNode != null) {
            Map<String, Object> values = getAnnotationValues(annotationNode, annotationClass);
            @SuppressWarnings("unchecked") List<Type> sets = (List<Type>) values.get(setsAnnotationField);
            for (Type redirectSetType : sets) {
                findRedirectSetsForType(redirectSetType, exceptions);
            }
        }
    }

    private void findRedirectSetsForType(Type redirectSetType, DasmWrappedExceptions exceptions) {
        RedirectSetImpl existingSet = this.redirectSetsByType.get(redirectSetType);
        if (existingSet == null) {
            try {
                ClassNode redirectSetClass = this.provider.classNode(redirectSetType);
                existingSet = RedirectSetImpl.parse(redirectSetClass, this.provider);
                this.redirectSetsByType.put(redirectSetType, existingSet);
            } catch (NoSuchTypeExists e) {
                exceptions.addException(e);
                return;
            } catch (DasmWrappedExceptions e) {
                exceptions.addNested(e);
                return;
            }
        }

        for (Type superRedirectSet : existingSet.superRedirectSets()) {
            findRedirectSetsForType(superRedirectSet, exceptions);
        }
    }
}
