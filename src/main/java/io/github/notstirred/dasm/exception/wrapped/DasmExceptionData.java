package io.github.notstirred.dasm.exception.wrapped;

import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.exception.EKind;
import io.github.notstirred.dasm.util.IndentingStringBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public abstract class DasmExceptionData {
    private final List<DasmExceptionData> nested = new ArrayList<>();
    private final List<DasmException> exceptions = new ArrayList<>();

    protected abstract String message();

    public void addException(DasmException e) {
        this.exceptions.add(e);
    }

    public <T extends DasmExceptionData> T addNested(T e) {
        this.nested.add(e);
        return e;
    }

    public boolean hasWrapped() {
        return this.hasWrapped(EKind.ERROR);
    }

    public boolean hasWrapped(EKind minimumKind) {
        return this.exceptions.stream().anyMatch(e -> e.kind.isAtLeast(minimumKind))
                || this.nested.stream().anyMatch(nested -> nested.hasWrapped(minimumKind));
    }

    /**
     * @return True if any exceptions remain
     */
    private boolean removeEmpty() {
        boolean hasWrappedException = false;
        for (Iterator<DasmExceptionData> iterator = this.nested.iterator(); iterator.hasNext(); ) {
            DasmExceptionData nested = iterator.next();
            if (!nested.removeEmpty()) {
                iterator.remove();
            } else {
                hasWrappedException = true;
            }
        }

        if (!this.exceptions.isEmpty()) {
            hasWrappedException = true;
        }
        return hasWrappedException;
    }

    public void throwIfHasWrapped() throws DasmException {
        if (this.removeEmpty()) {
            if (this.hasWrapped(EKind.ERROR)) {
                DasmException dasmException = new DasmException(exceptionMessage(new IndentingStringBuilder(4), EKind.ERROR));
                this.getCauses().forEach(dasmException::addSuppressed);
                throw dasmException;
            } else {
                System.err.println(exceptionMessage(new IndentingStringBuilder(4), EKind.INFO));
            }
        }
    }

    private Stream<DasmException> getCauses() {
        return Stream.concat(this.nested.stream().flatMap(DasmExceptionData::getCauses), this.exceptions.stream());
    }

    private String exceptionMessage(IndentingStringBuilder builder, EKind minimumKind) {
        builder.appendLine(this.message()).indent();

        this.nested.forEach(nested -> {
            if (nested.hasWrapped(minimumKind)) {
                nested.exceptionMessage(builder, minimumKind);
            }
        });

        this.exceptions.stream().filter(e -> e.kind.isAtLeast(minimumKind))
                .forEach(exception -> builder.appendLine(exception.getMessage()));

        builder.unindent();
        return builder.toString();
    }
}
