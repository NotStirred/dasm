package io.github.notstirred.dasm.test.targets;

public interface AFactory {
    static A createA() {
        return new A();
    }
}
