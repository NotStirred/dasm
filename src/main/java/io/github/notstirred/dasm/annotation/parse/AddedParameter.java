package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.transform.AddUnusedParam;
import lombok.Value;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.Map;

@Value
public class AddedParameter {
    Type type;

    public static AddedParameter parse(AnnotationNode annotation) throws RefImpl.RefAnnotationGivenNoArguments {
        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, AddUnusedParam.class);

        Type type = RefImpl.parseRefAnnotation("type", values);
        return new AddedParameter(type);
    }
}
