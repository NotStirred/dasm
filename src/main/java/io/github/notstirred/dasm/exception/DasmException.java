package io.github.notstirred.dasm.exception;

public class DasmException extends Exception {
    public DasmException(String message) {
        super(message);
    }

    public DasmException(String message, Throwable cause) {
        super(message, cause);
    }
}
