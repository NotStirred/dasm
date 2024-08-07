package io.github.notstirred.dasm.transformer.data;

import io.github.notstirred.dasm.annotation.parse.AddedParameter;
import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.api.annotations.transform.ApplicationStage;
import io.github.notstirred.dasm.data.ClassMethod;
import lombok.Data;

import java.util.List;

@Data
public class MethodTransform {
    private final ClassMethod srcMethod;
    private final String dstMethodName;
    private final List<RedirectSetImpl> redirectSets;
    private final ApplicationStage stage;
    private final boolean inPlace;
    private final List<AddedParameter> addedParameters;
}
