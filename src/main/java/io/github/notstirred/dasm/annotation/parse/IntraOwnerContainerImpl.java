package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.redirect.sets.IntraOwnerContainer;
import io.github.notstirred.dasm.util.NotifyStack;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.AnnotationUtil.getAnnotationValues;
import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;

@Data
public class IntraOwnerContainerImpl {
    private final Type type;

    public static Optional<IntraOwnerContainerImpl> parse(ClassNode classNode, NotifyStack classExceptions) {
        if (classNode.invisibleAnnotations == null) {
            return Optional.empty();
        }
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(classNode.invisibleAnnotations, IntraOwnerContainer.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = getAnnotationValues(annotation, IntraOwnerContainer.class);

        Type owner;
        try {
            owner = parseRefAnnotation("value", values);
        } catch (RefImpl.RefAnnotationGivenNoArguments e) {
            classExceptions.notifyFromException(e);
            return Optional.empty();
        }

        return Optional.of(new IntraOwnerContainerImpl(owner));
    }
}
