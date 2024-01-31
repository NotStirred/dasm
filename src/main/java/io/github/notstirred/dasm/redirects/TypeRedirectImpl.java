package io.github.notstirred.dasm.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.util.Validator;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;

@Data
public class TypeRedirectImpl {
    public final Type srcType;
    public final Type dstType;

    public static Optional<TypeRedirectImpl> parseTypeRedirect(ClassNode classNode, Validator validator)
            throws RefImpl.RefAnnotationGivenInvalidArguments {
        if (classNode.invisibleAnnotations == null) {
            return Optional.empty();
        }
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(classNode.invisibleAnnotations, TypeRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, TypeRedirect.class);

        @SuppressWarnings("unchecked") Type from = parseRefAnnotation((Map<String, Object>) values.get("from"), validator);
        @SuppressWarnings("unchecked") Type to = parseRefAnnotation((Map<String, Object>) values.get("to"), validator);

        return Optional.of(new TypeRedirectImpl(from, to));
    }
}
