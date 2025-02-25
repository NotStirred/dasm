package io.github.notstirred.dasm.exception.wrapped;

import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.util.IndentingStringBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

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
        return !this.exceptions.isEmpty() || this.nested.stream().anyMatch(DasmExceptionData::hasWrapped);
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
            DasmException dasmException = new DasmException(exceptionMessage(new IndentingStringBuilder(4)));
            this.forAllCauses(dasmException::addSuppressed);
            throw dasmException;
        }
    }

    private void forAllCauses(Consumer<DasmException> consumer) {
        this.nested.forEach(nested -> nested.forAllCauses(consumer));
        this.exceptions.forEach(consumer);
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
}
