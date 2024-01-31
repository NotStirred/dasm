package io.github.notstirred.dasm.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldRedirect;

import io.github.notstirred.dasm.exception.EmptyDstNameException;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.util.Validator;

import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.FieldNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;

@Data
public class FieldRedirectImpl {
    public final ClassField srcField;

    public final Optional<Type> dstOwner;
    public final String dstName;

    public static Optional<FieldRedirectImpl> parseFieldRedirect(Type fieldOwner, FieldNode fieldNode,
                                                                 Optional<Type> dstOwner, Validator validator)
            throws RefImpl.RefAnnotationGivenInvalidArguments, EmptyDstNameException {
        if (fieldNode.invisibleAnnotations == null) {
            return Optional.empty();
        }
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(fieldNode.invisibleAnnotations, FieldRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, FieldRedirect.class);

        String dstName = (String) values.get("value");
        if (dstName.isEmpty()) {
            throw new EmptyDstNameException();
        }

        @SuppressWarnings("unchecked") Type mappingsOwner = parseRefAnnotation((Map<String, Object>) values.get("mappingsOwner"), validator);

        // the field node exists, could we do a Type.getUnchecked?
        // though if the class isn't on the classpath (a library not shadowed) it would be invalid.
        Type fieldType = Type.getType(fieldNode.desc);
        return Optional.of(new FieldRedirectImpl(
                new ClassField(fieldOwner, mappingsOwner, fieldType, fieldNode.name),
                dstOwner,
                dstName
        ));
    }
}
