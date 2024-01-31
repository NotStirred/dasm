package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.api.annotations.selector.Ref;
import io.github.notstirred.dasm.util.Validator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Type;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RefImpl {
    public static class RefAnnotationGivenInvalidArguments extends Exception {}

    public static Type parseRefAnnotation(Map<String, Object> values, Validator validator) throws RefAnnotationGivenInvalidArguments {
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
            throw new RefAnnotationGivenInvalidArguments();
        }
        return type;
    }
}
