package io.github.notstirred.dasm.api.provider;

import java.util.Optional;

public interface ClassProvider {
    /**
     * @param className The fully qualified name of the class eg: {@code java.lang.String}
     * @return The class bytes, if the class exists.
     */
    Optional<byte[]> classBytes(String className);
}
