package io.github.notstirred.dasm.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.MethodRedirect;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;
import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotationAcceptEmpty;

@Data
public class MethodRedirectImpl {
    public final ClassMethod srcMethod;
    public final Type dstOwner;
    public final String dstName;

    public static Optional<MethodRedirectImpl> parseMethodRedirect(Type methodOwner, MethodNode methodNode,
                                                                   Type dstOwner)
            throws RefImpl.RefAnnotationGivenInvalidArguments, MethodRedirectHasEmptySrcName {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, MethodRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, MethodRedirect.class);

        Type ret = parseRefAnnotation((AnnotationNode) values.get("ret"));
        List<Type> args = new ArrayList<>();
        //noinspection unchecked
        for (AnnotationNode annotationNode : ((List<AnnotationNode>) values.get("args"))) {
            args.add(parseRefAnnotation(annotationNode));
        }
        String name = (String) values.get("name");

        if (name.isEmpty()) {
            throw new MethodRedirectHasEmptySrcName(methodNode);
        }

        Type mappingsOwner = parseRefAnnotationAcceptEmpty((AnnotationNode) values.get("mappingsOwner")).orElse(null);

        return Optional.of(new MethodRedirectImpl(
                new ClassMethod(methodOwner, mappingsOwner, new Method(name, Type.getMethodDescriptor(ret, args.toArray(new Type[0])))),
                dstOwner,
                methodNode.name
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
