package io.github.notstirred.dasm.annotation.parse.addtosets;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddMethodToSets;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseOptionalRefAnnotation;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

@Data
public class AddMethodToSetsImpl {
    private final List<Type> containers;

    private final Method srcMethod;
    private final Optional<Type> mappingsOwner;

    private final Type dstOwner;
    private final String dstMethodName;
    private final boolean isDstInterface;

    private final boolean isStatic;

    public static Optional<AddMethodToSetsImpl> parse(Type dstOwner, boolean isDstInterface, MethodNode methodNode)
            throws RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.InvalidMethodSignature, MethodSigImpl.EmptySrcName {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, AddMethodToSets.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, AddMethodToSets.class);

        Method srcMethod = MethodSigImpl.parse((AnnotationNode) values.get("method"));

        Optional<Type> mappingsOwner = parseOptionalRefAnnotation((AnnotationNode) values.get("mappingsOwner"));

        List<Type> containers = (List<Type>) values.get("containers");

        boolean isStatic = (methodNode.access & ACC_STATIC) != 0;

        return Optional.of(new AddMethodToSetsImpl(containers, srcMethod, mappingsOwner, dstOwner, methodNode.name, isDstInterface, isStatic));
    }
}
