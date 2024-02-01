package io.github.notstirred.dasm.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationValues;
import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;
import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotationAcceptEmpty;

@Data
public class FieldRedirectImpl {
    public final ClassField srcField;

    public final Type dstOwner;
    public final String dstName;

    public static Optional<FieldRedirectImpl> parseFieldRedirect(Type fieldOwner, FieldNode fieldNode, Type dstOwner)
            throws RefImpl.RefAnnotationGivenInvalidArguments, FieldRedirectHasEmptySrcName {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(fieldNode.invisibleAnnotations, FieldRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = getAnnotationValues(annotation, FieldRedirect.class);

        Type srcType = parseRefAnnotation((AnnotationNode) values.get("type"));
        String srcName = (String) values.get("name");

        if (srcName.isEmpty()) {
            throw new FieldRedirectHasEmptySrcName(fieldNode);
        }

        Type mappingsOwner = parseRefAnnotationAcceptEmpty((AnnotationNode) values.get("mappingsOwner")).orElse(null);

        return Optional.of(new FieldRedirectImpl(
                new ClassField(fieldOwner, mappingsOwner, srcType, srcName),
                dstOwner,
                fieldNode.name
        ));
    }

    public static class FieldRedirectHasEmptySrcName extends DasmAnnotationException {
        public final FieldNode fieldNode;

        public FieldRedirectHasEmptySrcName(FieldNode fieldNode) {
            this.fieldNode = fieldNode;
        }
    }

    public static class FieldMissingFieldRedirectAnnotationException extends DasmAnnotationException {
        public final FieldNode fieldNode;

        public FieldMissingFieldRedirectAnnotationException(FieldNode fieldNode) {
            this.fieldNode = fieldNode;
        }
    }
}
