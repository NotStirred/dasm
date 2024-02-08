package io.github.notstirred.dasm.exception.wrapped;

import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.util.IndentingStringBuilder;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class DasmWrappedExceptions extends DasmException {
    private final List<DasmWrappedExceptions> nested = new ArrayList<>();
    private final List<DasmException> exceptions = new ArrayList<>();

    public DasmWrappedExceptions() {
        super();
    }

    protected abstract String message();

    public void addException(DasmException e) {
        this.exceptions.add(e);
    }

    public <T extends DasmWrappedExceptions> T addNested(T e) {
        this.nested.add(e);
        return e;
    }

    public boolean hasWrapped() {
        return !this.exceptions.isEmpty() || this.nested.stream().anyMatch(DasmWrappedExceptions::hasWrapped);
    }

    public void throwIfHasWrapped() throws DasmWrappedExceptions {
        if (this.hasWrapped()) {
            throw this;
        }
    }


    private String exceptionMessage(IndentingStringBuilder builder) {
        builder.appendLine(this.message()).indent();

        this.nested.forEach(nested -> {
            if (nested.hasWrapped()) {
                nested.exceptionMessage(builder);
            }
        });

        this.exceptions.forEach(exception ->
                builder.appendLine(exception.getMessage())
        );

        builder.unindent();
        return builder.toString();
    }

    @Override public void printStackTrace() {
        System.err.println(exceptionMessage(new IndentingStringBuilder(4)));
    }

    @Override public void printStackTrace(PrintStream s) {
        s.println(exceptionMessage(new IndentingStringBuilder(4)));
    }

    @Override public void printStackTrace(PrintWriter s) {
        s.println(exceptionMessage(new IndentingStringBuilder(4)));
    }
}
