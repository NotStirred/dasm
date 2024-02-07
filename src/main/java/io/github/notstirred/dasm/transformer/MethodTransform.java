package io.github.notstirred.dasm.transformer;

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
}
