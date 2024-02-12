package io.github.notstirred.dasm.annotation.parse.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.ConstructorMethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.ConstructorToFactoryRedirect;
import io.github.notstirred.dasm.data.ClassMethod;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Map;
import java.util.Optional;

@Data
public class ConstructorToFactoryRedirectImpl {
    private final ClassMethod srcConstructor;
    private final Type dstOwner;
    private final String dstName;
    private final boolean isDstOwnerInterface;

    public static Optional<ConstructorToFactoryRedirectImpl> parse(Type methodOwner, boolean methodOwnerIsInterface, MethodNode methodNode, Type dstOwner)
            throws RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.InvalidMethodSignature, MethodSigImpl.EmptySrcName {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, ConstructorToFactoryRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, ConstructorToFactoryRedirect.class);

        Type[] args = ConstructorMethodSigImpl.parse((AnnotationNode) values.get("value"));

        return Optional.of(new ConstructorToFactoryRedirectImpl(
                new ClassMethod(methodOwner, new Method("<init>", Type.getType(void.class), args)),
                dstOwner,
                methodNode.name,
                methodOwnerIsInterface
        ));
    }
}
