package io.github.notstirred.dasm.exception.wrapped;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.ClassNode;

@RequiredArgsConstructor
public class DasmClassExceptions extends DasmWrappedExceptions {
    private final String message;

    public DasmClassExceptions(String message, ClassNode classNode) {
        this.message = message + " " + classNode.name.replace('/', '.').replace('$', '.');
    }

    @Override protected String message() {
        return message;
    }
}
