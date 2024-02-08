package io.github.notstirred.dasm.exception.wrapped;


import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.AnnotationNode;

@RequiredArgsConstructor
public class DasmAnnotationExceptions extends DasmWrappedExceptions {
    private final AnnotationNode annotationNode;

    @Override protected String message() {
        return annotationNode.desc
                .substring(1, annotationNode.desc.length() - 1)
                .replace('/', '.');
    }
}
