package io.github.notstirred.dasm.test.utils;

import java.util.function.Function;

public class ByteArrayClassLoader extends ClassLoader {
    private final Function<String, byte[]> classBytesLoader;

    public ByteArrayClassLoader(ClassLoader parent, Function<String, byte[]> classBytesLoader) {
        super(parent);
        this.classBytesLoader = classBytesLoader;
    }

    @Override
    protected Class<?> findClass(String name) {
        byte[] bytes = this.classBytesLoader.apply(name);
        return defineClass(name, bytes, 0, bytes.length);
    }
}
