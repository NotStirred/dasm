package io.github.notstirred.dasm.annotation.parse.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseOptionalRefAnnotation;

@Data
public class MethodRedirectImpl {
    private final ClassMethod srcMethod;
    private final Type dstOwner;
    private final String dstName;
    private final boolean isDstOwnerInterface;

    public static Optional<MethodRedirectImpl> parseMethodRedirect(Type methodOwner, boolean methodOwnerIsInterface, MethodNode methodNode,
                                                                   Type dstOwner)
            throws RefImpl.RefAnnotationGivenInvalidArguments, MethodRedirectHasEmptySrcName, MethodSigImpl.InvalidMethodSignature {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, MethodRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, MethodRedirect.class);

        Method srcMethod = MethodSigImpl.parse((AnnotationNode) values.get("value"));

        Type mappingsOwner = parseOptionalRefAnnotation((AnnotationNode) values.get("mappingsOwner")).orElse(methodOwner);

        return Optional.of(new MethodRedirectImpl(
                new ClassMethod(methodOwner, mappingsOwner, srcMethod),
                dstOwner,
                methodNode.name,
                methodOwnerIsInterface
        ));
    }

    public static class MethodRedirectHasEmptySrcName extends DasmAnnotationException {
        public final MethodNode methodNode;

        public MethodRedirectHasEmptySrcName(MethodNode methodNode) {
            this.methodNode = methodNode;
        }
    }

    public static class MethodMissingMethodRedirectAnnotationException extends DasmAnnotationException {
        public final MethodNode methodNode;

        public MethodMissingMethodRedirectAnnotationException(MethodNode methodNode) {
            this.methodNode = methodNode;
        }
    }
}
