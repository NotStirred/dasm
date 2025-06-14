package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.redirect.sets.InterOwnerContainer;
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
public class InterOwnerContainerImpl {
    private final Type srcType;
    private final Type dstType;

    public static Optional<InterOwnerContainerImpl> parse(ClassNode classNode, NotifyStack classExceptions) {
        if (classNode.invisibleAnnotations == null) {
            return Optional.empty();
        }
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(classNode.invisibleAnnotations, InterOwnerContainer.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = getAnnotationValues(annotation, InterOwnerContainer.class);

        Type owner;
        try {
            owner = parseRefAnnotation("from", values);
        } catch (RefImpl.RefAnnotationGivenNoArguments e) {
            classExceptions.notifyFromException(e);
            return Optional.empty();
        }
        Type newOwner = RefImpl.parseOptionalRefAnnotation(((AnnotationNode) values.get("to"))).orElse(owner);

        return Optional.of(new InterOwnerContainerImpl(owner, newOwner));
    }
}
