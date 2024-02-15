package io.github.notstirred.dasm.transformer.data;

import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.api.annotations.transform.ApplicationStage;
import lombok.Data;
import org.objectweb.asm.Type;

import java.util.List;

@Data
public class ClassTransform {
    private final Type srcType;
    private final Type dstType;
    private final List<RedirectSetImpl> redirectSets;
    private final ApplicationStage stage;
}
