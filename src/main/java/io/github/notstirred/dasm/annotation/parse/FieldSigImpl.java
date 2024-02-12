package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.selector.FieldSig;
import io.github.notstirred.dasm.data.Field;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FieldSigImpl {
    public static Field parse(AnnotationNode annotation) throws RefImpl.RefAnnotationGivenNoArguments {
        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, FieldSig.class);

        Type type = RefImpl.parseRefAnnotation("type", values);
        String name = (String) values.get("name");

        return new Field(type, name);
    }
}
