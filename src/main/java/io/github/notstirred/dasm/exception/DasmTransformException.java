package io.github.notstirred.dasm.exception;

public abstract class DasmTransformException extends DasmException {
    public DasmTransformException(String message) {
        super(message);
    }

    public DasmTransformException(String message, EKind kind) {
        super(message, kind);
    }
}
