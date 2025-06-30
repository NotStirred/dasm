package io.github.notstirred.dasm.annotation.parse.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.Field;
import io.github.notstirred.dasm.notify.Notification;
import io.github.notstirred.dasm.util.ReferenceUtil;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationValues;
import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseOptionalRefAnnotation;

@Data
public class FieldRedirectImpl {
    private final ClassField srcField;

    private final Type dstOwner;
    private final String dstName;

    public static Optional<FieldRedirectImpl> parseFieldRedirect(Type fieldOwner, FieldNode fieldNode, Type dstOwner)
            throws RefImpl.RefAnnotationGivenNoArguments, ReferenceUtil.InvalidReference {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(fieldNode.invisibleAnnotations, FieldRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = getAnnotationValues(annotation, FieldRedirect.class);

        Field field = ReferenceUtil.parseFieldReference(((String) values.get("value")));

        Type mappingsOwner = parseOptionalRefAnnotation((AnnotationNode) values.get("mappingsOwner")).orElse(fieldOwner);

        return Optional.of(new FieldRedirectImpl(
                new ClassField(fieldOwner, mappingsOwner, field.type(), field.name()),
                dstOwner,
                fieldNode.name
        ));
    }

    public static class FieldMissingFieldRedirectAnnotationException extends Notification {
        public FieldMissingFieldRedirectAnnotationException(FieldNode fieldNode) {
            super("Field is missing a @FieldRedirect annotation.");
        }
    }
}
