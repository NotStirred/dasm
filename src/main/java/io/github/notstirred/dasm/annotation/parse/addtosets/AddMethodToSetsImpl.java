package io.github.notstirred.dasm.annotation.parse.addtosets;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.util.Pair;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseOptionalRefAnnotation;
import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseRefAnnotation;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class AddMethodToSetsImpl {
    public static Optional<Pair<List<Type>, MethodRedirectImpl>> parse(Type dstOwner, boolean isDstInterface, MethodNode methodNode)
            throws RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.InvalidMethodSignature, MethodSigImpl.EmptySrcName {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, AddMethodToSets.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, AddMethodToSets.class);

        Type methodOwner = parseRefAnnotation("owner", values);

        Method srcMethod = MethodSigImpl.parse((AnnotationNode) values.get("method"));

        Type mappingsOwner = parseOptionalRefAnnotation((AnnotationNode) values.get("mappingsOwner")).orElse(methodOwner);

        List<Type> sets = (List<Type>) values.get("sets");

        return Optional.of(new Pair<>(sets, new MethodRedirectImpl(
                new ClassMethod(methodOwner, mappingsOwner, srcMethod),
                dstOwner,
                methodNode.name,
                (methodNode.access & ACC_STATIC) != 0,
                isDstInterface
        )));
    }
}
