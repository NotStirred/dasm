package io.github.notstirred.dasm.annotation;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.*;
import java.util.stream.Collectors;

import static io.github.notstirred.dasm.util.TypeUtil.classToDescriptor;
import static java.util.Optional.empty;
import static java.util.Optional.of;

public class AnnotationUtil {
    public static <T> Optional<List<T>> annotationElementAsList(Object listOrSingleElement) {
        if (listOrSingleElement == null) {
            return empty();
        }
        if (listOrSingleElement instanceof List) {
            return of((List<T>) listOrSingleElement);
        }
        return of(Lists.newArrayList((T) listOrSingleElement));
    }

    @Nullable
    public static AnnotationNode getAnnotationIfPresent(List<AnnotationNode> annotations, Class<?> annotation) {
        if (annotations == null) {
            return null;
        }
        for (AnnotationNode annotationNode : annotations) {
            if (annotationNode.desc.equals(classToDescriptor(annotation))) {
                return annotationNode;
            }
        }
        return null;
    }

    @NotNull
    public static List<AnnotationNode> getAllAnnotations(List<AnnotationNode> annotations, Class<?> annotation) {
        List<AnnotationNode> annotationsOfType = new ArrayList<>();
        if (annotations == null) {
            return annotationsOfType;
        }
        for (AnnotationNode annotationNode : annotations) {
            if (annotationNode.desc.equals(classToDescriptor(annotation))) {
                annotationsOfType.add(annotationNode);
            }
        }
        return annotationsOfType;
    }

    public static boolean isAnnotationPresent(List<AnnotationNode> annotations, Class<?> annotation) {
        if (annotations == null) {
            return false;
        }
        for (AnnotationNode annotationNode : annotations) {
            if (annotationNode.desc.equals(classToDescriptor(annotation))) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Object> getAnnotationValues(AnnotationNode annotationNode, Class<?> annotation) {
        Map<String, Object> annotationValues = new HashMap<>();

        // Insert specified arguments in the annotation
        if (annotationNode != null && annotationNode.values != null) {
            for (int i = 0; i < annotationNode.values.size(); i += 2) {
                String name = (String) annotationNode.values.get(i);
                Object value = annotationNode.values.get(i + 1);

                annotationValues.put(name, value);
            }
        }

        addMissingDefaultValues(annotation, annotationValues);
        return annotationValues;
    }

    private static void addMissingDefaultValues(Class<?> annotation, Map<String, Object> annotationValues) {
        // Insert default arguments, only if they aren't already present
        for (java.lang.reflect.Method declaredMethod : annotation.getDeclaredMethods()) {
            Object defaultValue = declaredMethod.getDefaultValue();
            if (defaultValue != null && !annotationValues.containsKey(declaredMethod.getName())) {
                if (declaredMethod.getReturnType().isAnnotation()) {
                    annotationValues.put(declaredMethod.getName(), null);
                } else {
                    annotationValues.put(declaredMethod.getName(), matchAsmTypes(defaultValue));
                }
            }
        }
    }

    private static Object matchAsmTypes(Object defaultValue) {
        if (defaultValue instanceof Class[]) {
            return Arrays.stream((Class<?>[]) defaultValue)
                    .map(Type::getType)
                    .collect(Collectors.toList());
        }
        if (defaultValue.getClass().isArray()) {
            return Arrays.asList((Object[]) defaultValue);
        }
        if (defaultValue instanceof Class<?>) {
            return Type.getType((Class<?>) defaultValue);
        }
        return defaultValue;
    }
}
