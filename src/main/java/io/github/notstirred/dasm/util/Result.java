package io.github.notstirred.dasm.util;

import java.util.Optional;
import java.util.function.Consumer;

public interface Result<L, R> {
    static <L, R> Result<L, R> ok(L l) {
        return new Ok<>(l);
    }

    static <L, R> Result<L, R> err(R r) {
        return new Err<>(r);
    }

    boolean isOk();

    Optional<L> ok();

    boolean isErr();

    Optional<R> err();

    void withOk(Consumer<L> cons);

    void withErr(Consumer<R> cons);

    class Ok<L, R> implements Result<L, R> {
        // never empty, just here to not allocate an optional every left()/right() call
        private final Optional<L> l;

        private Ok(L l) {
            this.l = Optional.of(l);
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public Optional<L> ok() {
            return this.l;
        }

        @Override
        public boolean isErr() {
            return false;
        }

        @Override
        public Optional<R> err() {
            return Optional.empty();
        }

        @Override
        public void withOk(Consumer<L> cons) {
            cons.accept(this.l.get());
        }

        @Override
        public void withErr(Consumer<R> cons) {
        }
    }

    class Err<L, R> implements Result<L, R> {
        // never empty, just here to not allocate an optional every left()/right() call
        private final Optional<R> r;

        private Err(R r) {
            this.r = Optional.of(r);
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public Optional<L> ok() {
            return Optional.empty();
        }

        @Override
        public boolean isErr() {
            return true;
        }

        @Override
        public Optional<R> err() {
            return this.r;
        }

        @Override
        public void withOk(Consumer<L> cons) {
        }

        @Override
        public void withErr(Consumer<R> cons) {
            cons.accept(this.r.get());
        }
    }
}
