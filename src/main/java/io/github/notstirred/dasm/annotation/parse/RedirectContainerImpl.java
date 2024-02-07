package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectContainer;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationValues;
import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;

@Data
public class RedirectContainerImpl {
    private final Type type;

    public static Optional<RedirectContainerImpl> parseRedirectContainer(ClassNode classNode)
            throws RefImpl.RefAnnotationGivenInvalidArguments {
        if (classNode.invisibleAnnotations == null) {
            return Optional.empty();
        }
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(classNode.invisibleAnnotations, RedirectContainer.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = getAnnotationValues(annotation, RedirectContainer.class);

        Type value = parseRefAnnotation((AnnotationNode) values.get("value"));

        return Optional.of(new RedirectContainerImpl(value));
    }
}
