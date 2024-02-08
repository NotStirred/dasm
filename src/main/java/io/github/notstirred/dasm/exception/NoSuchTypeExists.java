package io.github.notstirred.dasm.exception;

import org.objectweb.asm.Type;

public class NoSuchTypeExists extends DasmAnnotationException {
    public NoSuchTypeExists(Type type) {
        super("No such type `" + type.getClassName() + "` exists");
    }
}
