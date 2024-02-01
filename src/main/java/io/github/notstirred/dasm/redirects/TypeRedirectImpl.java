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

    public static Optional<TypeRedirectImpl> parseTypeRedirect(ClassNode classNode)
            throws RefImpl.RefAnnotationGivenInvalidArguments {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(classNode.invisibleAnnotations, TypeRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, TypeRedirect.class);

        Type from = parseRefAnnotation((AnnotationNode) values.get("from"));
        Type to = parseRefAnnotation((AnnotationNode) values.get("to"));

        return Optional.of(new TypeRedirectImpl(from, to));
    }
}
