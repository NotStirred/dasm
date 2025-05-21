package io.github.notstirred.dasm.transformer.data;

import io.github.notstirred.dasm.annotation.parse.AddedParameter;
import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.api.annotations.transform.ApplicationStage;
import io.github.notstirred.dasm.api.annotations.transform.Visibility;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.exception.DasmTransformException;
import lombok.Data;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

@Data
public class MethodTransform {
    private final ClassMethod srcMethod;
    private final String dstMethodName;
    private final List<RedirectSetImpl> redirectSets;
    private final ApplicationStage stage;
    private final boolean inPlace;

    private final TransformChanges transformChanges;

    private final OriginalTransformData originalTransformData;

    @Data
    public static class TransformChanges {
        private final List<AddedParameter> addedParameters;
        private final Visibility dstMethodVisibility;
        private final Visibility annotationVisibility;

        public void throwIfInvalidAccess(OriginalTransformData originalTransformData, Visibility srcMethodVisibility) throws DasmTransformException {
            if (this.annotationVisibility == Visibility.SAME_AS_TARGET) {
                // if the annotation is default, the dst must match the src method
                if (this.dstMethodVisibility != srcMethodVisibility) {
                    throw new InvalidVisibility(originalTransformData, this.dstMethodVisibility, srcMethodVisibility);
                } else {
                    return;
                }
            }

            // otherwise the dst method must match the annotation
            if (this.dstMethodVisibility != this.annotationVisibility) {
                throw new InvalidVisibility(originalTransformData, this.dstMethodVisibility, this.annotationVisibility);
            }
        }

        public static class InvalidVisibility extends DasmTransformException {
            public InvalidVisibility(OriginalTransformData originalTransformData, Visibility has, Visibility expected) {
                super("Transform " + originalTransformData.className.substring(originalTransformData.className.lastIndexOf('/') + 1)
                        + "#" + originalTransformData.methodNode.name + " has invalid visibility. Has `" + has + "`, expected `" + expected + "`");
            }
        }
    }

    @Data
    public static class OriginalTransformData {
        private final String className;
        private final MethodNode methodNode;
    }
}
