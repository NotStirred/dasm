package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.transform.ApplicationStage;
import io.github.notstirred.dasm.api.annotations.transform.TransformFromMethod;
import io.github.notstirred.dasm.api.annotations.transform.Visibility;
import lombok.Data;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
public class TransformFromMethodImpl {
    public static TransformMethodImpl parse(AnnotationNode annotation)
            throws MethodSigImpl.InvalidMethodSignature, RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.EmptySrcName {
        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, TransformFromMethod.class);

        Method srcMethod = MethodSigImpl.parse((AnnotationNode) values.get("value"));
        ApplicationStage stage = (ApplicationStage) values.get("stage");

        Visibility visibility;
        if (values.get("visibility") instanceof String[]) {
            visibility = Visibility.valueOf(Visibility.class, ((String[]) values.get("visibility"))[1]);
        } else {
            visibility = (Visibility) values.get("visibility");
        }
//        boolean makeSyntheticAccessor = (boolean) values.get("makeSyntheticAccessor");
        Optional<Type> owner = RefImpl.parseOptionalRefAnnotation((AnnotationNode) values.get("owner"));
        @SuppressWarnings("unchecked") List<Type> redirectSets = (List<Type>) values.get("useRedirectSets");

        Optional<List<Type>> overriddenRedirectSets = redirectSets.isEmpty() ? Optional.empty() : Optional.of(redirectSets);

        return new TransformMethodImpl(owner, srcMethod, stage, visibility, overriddenRedirectSets, false);
    }
}
