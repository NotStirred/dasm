package io.github.notstirred.dasm.annotation.parse.addtosets;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.FieldSigImpl;
import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.data.Field;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseOptionalRefAnnotation;

@Data
public class AddFieldToSetsImpl {
    private final List<Type> containers;

    private final Field srcField;
    private final Optional<Type> mappingsOwner;

    private final Type dstOwner;
    private final String dstFieldName;

    public static Optional<AddFieldToSetsImpl> parse(Type dstOwner, FieldNode fieldNode)
            throws RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.InvalidMethodSignature, MethodSigImpl.EmptySrcName {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(fieldNode.invisibleAnnotations, AddFieldToSets.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, AddFieldToSets.class);

        Field srcField = FieldSigImpl.parse((AnnotationNode) values.get("field"));

        Optional<Type> mappingsOwner = parseOptionalRefAnnotation((AnnotationNode) values.get("mappingsOwner"));

        List<Type> sets = (List<Type>) values.get("containers");

        return Optional.of(new AddFieldToSetsImpl(
                sets,
                srcField,
                mappingsOwner,
                dstOwner,
                fieldNode.name
        ));
    }
}
