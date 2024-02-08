package io.github.notstirred.dasm.exception.wrapped;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.FieldNode;

@RequiredArgsConstructor
public class DasmFieldExceptions extends DasmWrappedExceptions {
    private final FieldNode fieldNode;

    @Override protected String message() {
        return fieldNode.name.replace('/', '.');
    }
}
