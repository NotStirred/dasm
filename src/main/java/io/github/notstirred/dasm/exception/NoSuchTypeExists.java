package io.github.notstirred.dasm.exception;

import lombok.RequiredArgsConstructor;
import org.objectweb.asm.Type;

@RequiredArgsConstructor
public class NoSuchTypeExists extends DasmAnnotationException {
    public final Type type;
}
