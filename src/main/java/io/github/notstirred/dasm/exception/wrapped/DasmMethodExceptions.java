package io.github.notstirred.dasm.exception.wrapped;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.MethodNode;

@RequiredArgsConstructor
public class DasmMethodExceptions extends DasmWrappedExceptions {
    private final MethodNode methodNode;

    @Override protected String message() {
        return "Exceptions for method: " + methodNode.name + methodNode.desc;
    }
}
