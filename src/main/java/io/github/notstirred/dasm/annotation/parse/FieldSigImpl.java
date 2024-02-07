package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.data.Field;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldSigImpl {
    public static Optional<Field> parse(Map<String, Object> annotation) throws RefImpl.RefAnnotationGivenInvalidArguments {
        if (annotation == null) {
            return Optional.empty();
        }

        Type type = RefImpl.parseRefAnnotation((AnnotationNode) annotation.get("type"));
        String name = (String) annotation.get("name");

        return Optional.of(new Field(type, name));
    }
}
