package io.github.notstirred.dasm.annotation;

import io.github.notstirred.dasm.annotation.parse.*;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddFieldToSetsImpl;
import io.github.notstirred.dasm.annotation.parse.addtosets.AddMethodToSetsImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.TypeRedirectImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.transform.*;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmFieldExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmMethodExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.transformer.data.ClassTransform;
import io.github.notstirred.dasm.transformer.data.MethodTransform;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import io.github.notstirred.dasm.util.TypeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.*;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class AnnotationParser {
    private final ClassNodeProvider provider;
    private final Map<Type, RedirectSetImpl> redirectSetsByType = new HashMap<>();

    public AnnotationParser(ClassNodeProvider provider) {
        this.provider = provider;
    }

    public void findRedirectSets(ClassNode targetClass) throws DasmWrappedExceptions {
        Type targetClassType = Type.getType(TypeUtil.typeNameToDescriptor(targetClass.name));
        boolean isTargetInterface = (targetClass.access & Opcodes.ACC_INTERFACE) != 0;

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
                AddMethodToSetsImpl.parse(targetClassType, isTargetInterface, methodNode).ifPresent(setsRedirectPair -> {
                    // All redirect sets for this method must already exist, so we can just use the map
                    setsRedirectPair.first().forEach(setType -> this.redirectSetsByType.get(setType).methodRedirects().add(setsRedirectPair.second()));
                });
            } catch (RefImpl.RefAnnotationGivenNoArguments | MethodSigImpl.InvalidMethodSignature | MethodSigImpl.EmptySrcName e) {
                methodExceptions.addException(e);
            }
        }

        classExceptions.throwIfHasWrapped();
    }

    public Optional<ClassTransform> buildClassTarget(ClassNode targetClass) throws DasmWrappedExceptions {
        Type targetType = Type.getType(TypeUtil.typeNameToDescriptor(targetClass.name));
        DasmClassExceptions classExceptions = new DasmClassExceptions("An exception occurred when looking for transforms in", targetClass);

        AnnotationNode transformFromClassNode = getAnnotationIfPresent(targetClass.invisibleAnnotations, TransformFromClass.class);
        if (transformFromClassNode != null) {
            Map<String, Object> values = getAnnotationValues(transformFromClassNode, TransformFromClass.class);

            try {
                Type srcType = RefImpl.parseRefAnnotation("value", values);
                ApplicationStage stage = (ApplicationStage) values.get("stage");

                List<RedirectSetImpl> sets = unrollSets(
                        AnnotationUtil.<Type>annotationElementAsList(values.get("sets")).orElseGet(ArrayList::new).stream()
                                .map(this.redirectSetsByType::get)
                                .collect(Collectors.toList())
                );

                AnnotationNode addToSetsAnnotation = getAnnotationIfPresent(targetClass.invisibleAnnotations, AddTransformToSets.class);
                if (addToSetsAnnotation != null) {
                    Map<String, Object> addToSets = getAnnotationValues(addToSetsAnnotation, AddTransformToSets.class);
                    List<Type> addTo = (List<Type>) addToSets.get("value");

                    addTo.forEach(set -> this.redirectSetsByType.get(set).typeRedirects()
                            .add(new TypeRedirectImpl(srcType, targetType, (targetClass.access & ACC_INTERFACE) != 0)));
                }

                // FIXME: this should verify that there are no method transforms inside this class,
                return Optional.of(new ClassTransform(srcType, targetType, sets, stage));
            } catch (RefImpl.RefAnnotationGivenNoArguments e) {
                classExceptions.addException(e);
            }
        }

        classExceptions.throwIfHasWrapped();
        return Optional.empty();
    }

    public Optional<Collection<MethodTransform>> buildMethodTargets(ClassNode targetClass, String methodPrefix) throws DasmWrappedExceptions {
        Type targetType = Type.getType(TypeUtil.typeNameToDescriptor(targetClass.name));
        boolean isTargetTypeInterface = (targetClass.access & Opcodes.ACC_INTERFACE) != 0;

        DasmClassExceptions classExceptions = new DasmClassExceptions("An exception occurred when looking for transforms in", targetClass);

        AnnotationNode dasmNode = getAnnotationIfPresent(targetClass.invisibleAnnotations, Dasm.class);
        if (dasmNode != null) {
            Map<String, Object> values = getAnnotationValues(dasmNode, Dasm.class);
            @SuppressWarnings("unchecked")
            List<RedirectSetImpl> defaultRedirectSets = unrollSets(((List<Type>) values.get("value")).stream()
                    .map(this.redirectSetsByType::get).collect(Collectors.toList())
            );

            List<MethodTransform> methodTransforms = new ArrayList<>();

            for (MethodNode method : targetClass.methods) {
                DasmMethodExceptions methodExceptions = classExceptions.addNested(new DasmMethodExceptions(method));

                TransformMethodImpl transformMethod = parseTransformMethod(method, methodExceptions);
                if (transformMethod == null)
                    continue;

                Type methodOwner = transformMethod.owner().orElse(targetType);

                List<RedirectSetImpl> redirectSets = transformMethod.overriddenRedirectSets().map(types ->
                                types.stream().map(this.redirectSetsByType::get).collect(Collectors.toList()))
                        .map(this::unrollSets)
                        .orElse(defaultRedirectSets);

                // FIXME: figure out if there is a way to avoid this with mixin.
                // Name is modified here to prevent mixin from overwriting it. We remove this prefix in postApply.
                // We redirect to the non-prefixed name, but create the method with the prefix, for later removal.
                String nonPrefixedMethodName = method.name;
                String prefixedMethodName = methodPrefix + nonPrefixedMethodName;

                List<AddedParameter> addedParameters = getAddedParameters(method, methodExceptions);

                MethodTransform transform = new MethodTransform(
                        new ClassMethod(methodOwner, methodOwner, transformMethod.srcMethod()),
                        prefixedMethodName // We have to rename constructors because we add a prefix, and mixin expects that anything with <> is either init, or clinit
                                .replace("<init>", "__init__")
                                .replace("<clinit>", "__clinit__"),
                        redirectSets,
                        transformMethod.stage(),
                        transformMethod.inPlace(),
                        addedParameters
                );

                AnnotationNode addToSetsAnnotation = getAnnotationIfPresent(method.invisibleAnnotations, AddTransformToSets.class);
                if (addToSetsAnnotation != null) {
                    Map<String, Object> addToSets = getAnnotationValues(addToSetsAnnotation, AddTransformToSets.class);
                    List<Type> sets = (List<Type>) addToSets.get("value");

                    sets.forEach(set -> this.redirectSetsByType.get(set).methodRedirects().add(new MethodRedirectImpl(
                            transform.srcMethod(),
                            targetType,
                            nonPrefixedMethodName,
                            (method.access & ACC_STATIC) != 0,
                            isTargetTypeInterface
                    )));
                }

                methodTransforms.add(transform);
            }
            classExceptions.throwIfHasWrapped();
            return Optional.of(methodTransforms);
        }

        classExceptions.throwIfHasWrapped();
        return Optional.empty();
    }

    /**
     * @param method           The method to look for annotations on
     * @param methodExceptions The object to add nested exceptions to if they occur
     * @return The requested parameters to add to the transformed method in order.
     */
    @NotNull
    private static List<AddedParameter> getAddedParameters(MethodNode method, DasmMethodExceptions methodExceptions) {
        List<AnnotationNode> addUnusedParamAnnotations = getAllAnnotations(method.invisibleAnnotations, AddUnusedParam.class);
        List<AddedParameter> addedParameters = new ArrayList<>();
        for (AnnotationNode annotation : addUnusedParamAnnotations) {
            try {
                addedParameters.add(AddedParameter.parse(annotation));
            } catch (RefImpl.RefAnnotationGivenNoArguments e) {
                methodExceptions.addException(e);
            }
        }
        return addedParameters;
    }

    /**
     * @param method          The method on which to look for annotations
     * @param methodExceptions The object to add nested exceptions to if they occur
     * @return null if there was no annotation or there was an exception
     */
    @Nullable
    private static TransformMethodImpl parseTransformMethod(MethodNode method, DasmMethodExceptions methodExceptions) {
        TransformMethodImpl transformMethod;
        AnnotationNode transformFromMethodAnnotation = getAnnotationIfPresent(method.invisibleAnnotations, TransformFromMethod.class);
        AnnotationNode transformMethodAnnotation = getAnnotationIfPresent(method.invisibleAnnotations, TransformMethod.class);
        if (transformFromMethodAnnotation == null && transformMethodAnnotation == null) {
            return null;
        }
        if (transformFromMethodAnnotation != null && transformMethodAnnotation != null) {
            methodExceptions.addException(new BothTransformMethodAndTransformFromMethodPresent(method));
            return null;
        }

        try {
            if (transformFromMethodAnnotation != null) {
                transformMethod = TransformFromMethodImpl.parse(transformFromMethodAnnotation);
            } else {
                transformMethod = TransformMethodImpl.parse(transformMethodAnnotation);
            }
        } catch (MethodSigImpl.InvalidMethodSignature | RefImpl.RefAnnotationGivenNoArguments |
                 MethodSigImpl.EmptySrcName e) {
            methodExceptions.addException(e);
            return null;
        }
        return transformMethod;
    }

    private void findRedirectSetsForAnnotation(List<AnnotationNode> annotations, Class<?> annotationClass, String setsAnnotationField,
                                               DasmWrappedExceptions exceptions) {
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

    /**
     * @return A collection of all redirect sets and super redirect sets in depth first order
     */
    private List<RedirectSetImpl> unrollSets(Collection<RedirectSetImpl> redirectSets) {
        List<RedirectSetImpl> unrolledSets = new ArrayList<>();
        redirectSets.forEach(set -> unrollSetsInner(set, unrolledSets));
        return unrolledSets;
    }

    private void unrollSetsInner(RedirectSetImpl redirectSet, Collection<RedirectSetImpl> out) {
        redirectSet.superRedirectSets().stream().map(this.redirectSetsByType::get).forEach(set -> unrollSetsInner(set, out));
        out.add(redirectSet);
    }

    public static class BothTransformMethodAndTransformFromMethodPresent extends DasmException {
        public BothTransformMethodAndTransformFromMethodPresent(MethodNode methodNode) {
            super(String.format("Method %s has both TransformMethod and TransformFromMethod annotations.", methodNode.name));
        }
    }
}
