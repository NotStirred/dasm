package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.api.annotations.Dasm;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationValues;

@Data
public class DasmImpl {
    private final Optional<Type> target;

    public static DasmImpl parse(AnnotationNode dasmNode) {
        Map<String, Object> values = getAnnotationValues(dasmNode, Dasm.class);
        Optional<Type> targetType = RefImpl.parseOptionalRefAnnotation((AnnotationNode) values.get("target"))
                .map(type -> type.getClassName().equals(Dasm.SELF_TARGET.class.getName()) ? null : type);

        return new DasmImpl(targetType);
    }
}
