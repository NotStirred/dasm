package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.transform.ApplicationStage;
import io.github.notstirred.dasm.api.annotations.transform.TransformMethod;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class TransformMethodImpl {
    private final Optional<Type> owner;
    private final Method srcMethod;
    private final ApplicationStage stage;
    private final Optional<List<Type>> overriddenRedirectSets;
    private final boolean inPlace;

    public static TransformMethodImpl parse(AnnotationNode annotation)
            throws MethodSigImpl.InvalidMethodSignature, RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.EmptySrcName {
        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, TransformMethod.class);

        Method srcMethod = MethodSigImpl.parse((AnnotationNode) values.get("value"));
        ApplicationStage stage = (ApplicationStage) values.get("stage");
//        boolean makeSyntheticAccessor = (boolean) values.get("makeSyntheticAccessor");
        @SuppressWarnings("unchecked") List<Type> redirectSets = (List<Type>) values.get("useRedirectSets");

        Optional<List<Type>> overriddenRedirectSets = redirectSets.isEmpty() ? Optional.empty() : Optional.of(redirectSets);

        return new TransformMethodImpl(Optional.empty(), srcMethod, stage, overriddenRedirectSets, true);
    }
}
