package io.github.notstirred.dasm.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.exception.EmptyDstNameException;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.util.Validator;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;

@Data
public class MethodRedirectImpl {
    public final ClassMethod srcMethod;
    public final Optional<Type> dstOwner;
    public final String dstName;

    public static Optional<MethodRedirectImpl> parseMethodRedirect(Type methodOwner, MethodNode methodNode,
                                                                   Optional<Type> dstOwner, Validator validator)
            throws RefImpl.RefAnnotationGivenInvalidArguments, EmptyDstNameException {
        if (methodNode.invisibleAnnotations == null) {
            return Optional.empty();
        }
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, MethodRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, MethodRedirect.class);

        String dstName = (String) values.get("value");
        if (dstName.isEmpty()) {
            throw new EmptyDstNameException();
        }

        @SuppressWarnings("unchecked") Type mappingsOwner = parseRefAnnotation((Map<String, Object>) values.get("mappingsOwner"), validator);

        return Optional.of(new MethodRedirectImpl(
                new ClassMethod(methodOwner, mappingsOwner, new Method(methodNode.name, methodNode.desc)),
                dstOwner,
                dstName
        ));
    }
}
