package io.github.notstirred.dasm.exception.wrapped;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.FieldNode;

@RequiredArgsConstructor
public class DasmFieldExceptions extends DasmExceptionData {
    private final FieldNode fieldNode;

    @Override protected String message() {
        return "Exceptions for field: " + fieldNode.name + fieldNode.desc;
    }
}
