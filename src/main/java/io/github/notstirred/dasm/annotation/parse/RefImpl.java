package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.util.TypeUtil.classNameToDescriptor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefImpl {
    public static Type parseRefAnnotation(AnnotationNode annotationNode) throws RefAnnotationGivenInvalidArguments {
        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotationNode, Ref.class);
        Type type = null;
        if (values.containsKey("value")) {
            type = (Type) values.get("value");
        }
        if (values.containsKey("string")) {
            String string = (String) values.get("string");
            if (!string.isEmpty()) {
                type = Type.getType(classNameToDescriptor(string));
            }
        }
        if (type == null || type.getClassName().equals(Ref.EmptyRef.class.getName())) {
            // there is a ref annotation, but it was empty:
            throw new RefAnnotationGivenInvalidArguments(annotationNode);
        }
        return type;
    }

    public static Optional<Type> parseOptionalRefAnnotation(AnnotationNode annotationNode) {
        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotationNode, Ref.class);
        Type type = null;
        if (values.containsKey("value")) {
            type = (Type) values.get("value");
        }
        if (values.containsKey("string")) {
            String string = (String) values.get("string");
            if (!string.isEmpty()) {
                type = Type.getObjectType(string);
            }
        }
        if (type == null || type.getClassName().equals(Ref.EmptyRef.class.getName())) {
            // there is a ref annotation, but it was empty:
            return Optional.empty();
        }
        return Optional.of(type);
    }

    public static class RefAnnotationGivenInvalidArguments extends DasmAnnotationException {
        public final AnnotationNode annotationNode;

        public RefAnnotationGivenInvalidArguments(AnnotationNode annotationNode) {
            this.annotationNode = annotationNode;
        }
    }
}
