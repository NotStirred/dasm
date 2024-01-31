package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectContainer;
import io.github.notstirred.dasm.util.Validator;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;

@Data
public class RedirectContainerImpl {
    public final Type srcType;
    public final Type dstType;

    public static Optional<RedirectContainerImpl> parseRedirectContainer(ClassNode classNode, Validator validator)
            throws RefImpl.RefAnnotationGivenInvalidArguments {
        if (classNode.invisibleAnnotations == null) {
            return Optional.empty();
        }
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(classNode.invisibleAnnotations, RedirectContainer.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, RedirectContainer.class);

        @SuppressWarnings("unchecked") Type from = parseRefAnnotation((Map<String, Object>) values.get("from"), validator);
        @SuppressWarnings("unchecked") Type to = parseRefAnnotation((Map<String, Object>) values.get("to"), validator);

        return Optional.of(new RedirectContainerImpl(from, to));
    }
}
