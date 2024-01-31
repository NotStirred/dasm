package io.github.notstirred.dasm.annotation;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.notstirred.dasm.util.TypeUtil.classDescriptorToClassName;
import static io.github.notstirred.dasm.util.TypeUtil.classToDescriptor;

public class AnnotationUtil {
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

    public boolean isAnnotationIfPresent(List<AnnotationNode> annotations, Class<?> annotation) {
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
        for (int i = 0; i < annotationNode.values.size(); i += 2) {
            String name = (String) annotationNode.values.get(i);
            Object value = annotationNode.values.get(i + 1);

            if (value instanceof AnnotationNode) {
                AnnotationNode annotationValue = (AnnotationNode) value;
                try {
                    value = getAnnotationValues(annotationValue, Class.forName(classDescriptorToClassName(annotationValue.desc)));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
            annotationValues.put(name, value);
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
                    Map<String, Object> values = new HashMap<>();
                    for (java.lang.reflect.Method method : defaultValue.getClass().getInterfaces()[0].getDeclaredMethods()) {
                        try {
                            if (method.getReturnType().isAnnotation()) {
                                @SuppressWarnings("unchecked") Map<String, Object> v = (Map<String, Object>) annotationValues.computeIfAbsent(method.getName(), n -> new HashMap<>());
                                addMissingDefaultValues(method.getReturnType(), v);
                            } else {
                                Object v = method.invoke(defaultValue);
                                values.put(method.getName(), matchAsmTypes(v));
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    annotationValues.put(declaredMethod.getName(), values);
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
