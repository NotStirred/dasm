package io.github.notstirred.dasm.annotation.parse.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.TypeRedirect;
import io.github.notstirred.dasm.util.NotifyStack;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;
import static org.objectweb.asm.Opcodes.ACC_INTERFACE;

@Data
public class TypeRedirectImpl {
    private final Type srcType;
    private final Type dstType;
    private final boolean isDstInterface;

    public static Optional<TypeRedirectImpl> parse(ClassNode classNode, NotifyStack methodExceptions) {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(classNode.invisibleAnnotations, TypeRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, TypeRedirect.class);

        Type from = null;
        try {
            from = parseRefAnnotation("from", values);
        } catch (RefImpl.RefAnnotationGivenNoArguments e) {
            methodExceptions.notifyFromException(e);
        }
        Type to = null;
        try {
            to = parseRefAnnotation("to", values);
        } catch (RefImpl.RefAnnotationGivenNoArguments e) {
            methodExceptions.notifyFromException(e);
        }

        if (to == null || from == null) {
            return Optional.empty();
        }

        return Optional.of(new TypeRedirectImpl(from, to, (classNode.access & ACC_INTERFACE) != 0));
    }
}
