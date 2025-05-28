package io.github.notstirred.dasm.exception;

public class DasmException extends Exception {
    public final EKind kind;

    public DasmException(String message) {
        super(message);
        this.kind = EKind.ERROR;
    }

    public DasmException(String message, EKind kind) {
        super(message);
        this.kind = kind;
    }
}
