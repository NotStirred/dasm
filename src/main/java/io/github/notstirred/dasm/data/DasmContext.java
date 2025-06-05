package io.github.notstirred.dasm.data;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.*;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddTransformToSets;
import io.github.notstirred.dasm.api.annotations.transform.*;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
import io.github.notstirred.dasm.exception.wrapped.DasmMethodExceptions;
import io.github.notstirred.dasm.transformer.data.ClassTransform;
import io.github.notstirred.dasm.transformer.data.MethodTransform;
import io.github.notstirred.dasm.util.Pair;
import io.github.notstirred.dasm.util.TypeUtil;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationIfPresent;
import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationValues;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

@RequiredArgsConstructor
public class DasmContext {
    private final Map<Type, RedirectSetImpl> redirectSetsByType;
    private final Map<Type, RedirectSetImpl.Container> containersByType;

    public Optional<ClassTransform> buildClassTarget(ClassNode targetClass) throws DasmException {
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

                // FIXME: this should verify that there are no method transforms inside this class,
                return Optional.of(new ClassTransform(srcType, targetType, sets, stage));
            } catch (RefImpl.RefAnnotationGivenNoArguments e) {
                classExceptions.addException(e);
            }
        }

        classExceptions.throwIfHasWrapped();
        return Optional.empty();
    }

    public Optional<Collection<MethodTransform>> buildMethodTargets(ClassNode dasmClass, String methodPrefix) throws DasmException {
        boolean isTargetTypeInterface = (dasmClass.access & Opcodes.ACC_INTERFACE) != 0;

        DasmClassExceptions classExceptions = new DasmClassExceptions("An exception occurred when looking for transforms in", dasmClass);

        AnnotationNode dasmNode = getAnnotationIfPresent(dasmClass.invisibleAnnotations, Dasm.class);
        if (dasmNode != null) {
            Map<String, Object> values = getAnnotationValues(dasmNode, Dasm.class);
            Type targetType = RefImpl.parseOptionalRefAnnotation((AnnotationNode) values.get("target"))
                    .map(type -> type.getClassName().equals(Dasm.SELF_TARGET.class.getName()) ? null : type)
                    .orElseGet(() -> Type.getType(TypeUtil.typeNameToDescriptor(dasmClass.name)));

            @SuppressWarnings("unchecked")
            List<RedirectSetImpl> defaultRedirectSets = unrollSets(((List<Type>) values.get("value")).stream()
                    .map(type -> {
                        RedirectSetImpl redirectSet = this.redirectSetsByType.get(type);
                        if (redirectSet == null) {
                            classExceptions.addException(new NoSuchTypeExists(type));
                        }
                        return redirectSet;
                    }).filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );

            List<MethodTransform> methodTransforms = new ArrayList<>();

            for (MethodNode method : dasmClass.methods) {
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

                Pair<Visibility, Visibility> visibility = getRequestedVisibility(method, transformMethod.visibility(), methodExceptions);

                MethodTransform transform = new MethodTransform(
                        new ClassMethod(methodOwner, methodOwner, transformMethod.srcMethod()),
                        prefixedMethodName // We have to rename constructors because we add a prefix, and mixin expects that anything with <> is either init, or clinit
                                .replace("<init>", "__init__")
                                .replace("<clinit>", "__clinit__"),
                        redirectSets,
                        transformMethod.stage(),
                        transformMethod.inPlace(),
                        new MethodTransform.TransformChanges(
                                addedParameters,
                                visibility.first,
                                visibility.second
                        ),
                        new MethodTransform.OriginalTransformData(targetType.getInternalName(), method)
                );

                AnnotationNode addToSetsAnnotation = getAnnotationIfPresent(method.invisibleAnnotations, AddTransformToSets.class);
                if (addToSetsAnnotation != null) {
                    Map<String, Object> addToSets = getAnnotationValues(addToSetsAnnotation, AddTransformToSets.class);
                    List<Type> sets = (List<Type>) addToSets.get("value");

                    sets.forEach(set -> this.containersByType.get(set).methodRedirects().add(new MethodRedirectImpl(
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

    private Pair<Visibility, Visibility> getRequestedVisibility(MethodNode method, Visibility annotationVisibility, DasmMethodExceptions methodExceptions) {
        Visibility methodVisibility = Visibility.fromAccess(method.access);
        if (annotationVisibility != Visibility.SAME_AS_TARGET && methodVisibility != annotationVisibility) {
            methodExceptions.addException(new TransformAndMethodVisibilityDiffer(method, annotationVisibility, methodVisibility));
        }
        return new Pair<>(methodVisibility, annotationVisibility);
    }

    /**
     * @param method           The method to look for annotations on
     * @param methodExceptions The object to add nested exceptions to if they occur
     * @return The requested parameters to add to the transformed method in order.
     */
    @NotNull
    private static List<AddedParameter> getAddedParameters(MethodNode method, DasmMethodExceptions methodExceptions) {
        List<AddedParameter> addedParameters = new ArrayList<>();

        List<AnnotationNode>[] invisibleParameterAnnotations = method.invisibleParameterAnnotations;
        if (invisibleParameterAnnotations == null)
            return addedParameters;

        Type[] argumentTypes = Type.getArgumentTypes(method.desc);
        for (int i = 0; i < invisibleParameterAnnotations.length; i++) {
            List<AnnotationNode> invisibleParameterAnnotation = invisibleParameterAnnotations[i];
            if (invisibleParameterAnnotation != null) {
                if (AnnotationUtil.isAnnotationPresent(invisibleParameterAnnotation, AddUnusedParam.class)) {
                    addedParameters.add(new AddedParameter(argumentTypes[i], i));
                }
            }
        }
        return addedParameters;
    }

    /**
     * @param method           The method on which to look for annotations
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

    public static class TransformAndMethodVisibilityDiffer extends DasmException {
        public TransformAndMethodVisibilityDiffer(MethodNode methodNode, Visibility transformVisibility, Visibility methodVisibility) {
            super(String.format("Method %s and its transform have different visibility, %s vs %s", methodNode.name, methodVisibility, transformVisibility));
        }
    }
}
