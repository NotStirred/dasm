package io.github.notstirred.dasm.util;

import java.util.Optional;

public interface Either<L, R> {
    static <L, R> Either<L, R> left(L l) {
        return new Left<>(l);
    }

    static <L, R> Either<L, R> right(R r) {
        return new Right<>(r);
    }

    boolean isLeft();

    Optional<L> left();


    boolean isRight();

    Optional<R> right();

    class Left<L, R> implements Either<L, R> {
        // never empty, just here to not allocate an optional every left()/right() call
        private final Optional<L> l;

        private Left(L l) {
            this.l = Optional.of(l);
        }

        @Override public boolean isLeft() {
            return true;
        }

        @Override public Optional<L> left() {
            return this.l;
        }

        @Override public boolean isRight() {
            return false;
        }

        @Override public Optional<R> right() {
            return Optional.empty();
        }
    }

    class Right<L, R> implements Either<L, R> {
        // never empty, just here to not allocate an optional every left()/right() call
        private final Optional<R> r;

        private Right(R r) {
            this.r = Optional.of(r);
        }

        @Override public boolean isLeft() {
            return false;
        }

        @Override public Optional<L> left() {
            return Optional.empty();
        }

        @Override public boolean isRight() {
            return true;
        }

        @Override public Optional<R> right() {
            return this.r;
        }
    }
}
