package io.github.notstirred.dasm.annotation.parse.addtosets;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.FieldSigImpl;
import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToSets;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.Field;
import io.github.notstirred.dasm.util.Pair;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseOptionalRefAnnotation;
import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;

public class AddFieldToSetsImpl {
    public static Optional<Pair<List<Type>, FieldRedirectImpl>> parse(Type dstOwner, FieldNode methodNode)
            throws RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.InvalidMethodSignature, MethodSigImpl.EmptySrcName {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, AddFieldToSets.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, AddFieldToSets.class);

        Type methodOwner = parseRefAnnotation("owner", values);

        Field srcField = FieldSigImpl.parse((AnnotationNode) values.get("field"));

        Type mappingsOwner = parseOptionalRefAnnotation((AnnotationNode) values.get("mappingsOwner")).orElse(methodOwner);

        List<Type> sets = (List<Type>) values.get("containers");

        return Optional.of(new Pair<>(sets, new FieldRedirectImpl(
                new ClassField(methodOwner, mappingsOwner, srcField.type(), srcField.name()),
                dstOwner,
                methodNode.name
        )));
    }
}
