package io.github.notstirred.dasm.notify;

public class Notification {
    public String message;

    public final Kind kind;

    public final Class<?> sourceClass;

    public Notification(String message) {
        this(message, Kind.ERROR);
    }

    public Notification(String message, Kind kind) {
        this.message = message;
        this.kind = kind;
        this.sourceClass = this.getClass();
    }

    public Notification(String message, Kind kind, Class<?> exceptionClass) {
        this.message = message;
        this.kind = kind;
        this.sourceClass = exceptionClass;
    }

    public enum Kind {
        INFO,
        WARNING,
        ERROR;

        public boolean isAtLeast(Kind kind) {
            return this.ordinal() >= kind.ordinal();
        }
    }
}
