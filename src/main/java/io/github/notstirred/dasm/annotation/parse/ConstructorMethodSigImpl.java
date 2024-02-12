package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.selector.ConstructorMethodSig;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstructorMethodSigImpl {
    public static Type[] parse(
            AnnotationNode annotation) throws MethodSigImpl.InvalidMethodSignature, RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.EmptySrcName {
        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, ConstructorMethodSig.class);

        @SuppressWarnings("unchecked") List<AnnotationNode> argAnnotations = ((List<AnnotationNode>) values.get("args"));
        List<Type> args = new ArrayList<>();
        for (AnnotationNode argAnnotation : argAnnotations) {
            args.add(RefImpl.parseUnnamedRefAnnotation(argAnnotation));
        }

        return args.toArray(new Type[0]);
    }
}
