package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectContainer;
import io.github.notstirred.dasm.exception.wrapped.DasmClassExceptions;
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
    private final Type srcType;
    private final Type dstType;

    public static Optional<RedirectContainerImpl> parseRedirectContainer(ClassNode classNode, DasmClassExceptions classExceptions) {
        if (classNode.invisibleAnnotations == null) {
            return Optional.empty();
        }
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(classNode.invisibleAnnotations, RedirectContainer.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = getAnnotationValues(annotation, RedirectContainer.class);

        Type owner;
        try {
            owner = parseRefAnnotation("owner", values);
        } catch (RefImpl.RefAnnotationGivenNoArguments e) {
            classExceptions.addException(e);
            return Optional.empty();
        }
        Type newOwner = RefImpl.parseOptionalRefAnnotation(((AnnotationNode) values.get("newOwner"))).orElse(owner);

        return Optional.of(new RedirectContainerImpl(owner, newOwner));
    }
}
