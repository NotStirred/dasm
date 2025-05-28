package io.github.notstirred.dasm.exception;

public enum EKind {
    INFO,
    WARNING,
    ERROR;

    public boolean isAtLeast(EKind kind) {
        return this.ordinal() >= kind.ordinal();
    }
}
