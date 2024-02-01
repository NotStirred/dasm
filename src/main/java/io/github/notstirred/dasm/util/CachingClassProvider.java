package io.github.notstirred.dasm.util;

import io.github.notstirred.dasm.api.provider.ClassProvider;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.ASM9;

public class CachingClassProvider implements ClassNodeProvider {
    private final ClassProvider classProvider;
    private final Map<Type, Optional<ClassNode>> classNodeCache = new HashMap<>();

    public CachingClassProvider(ClassProvider classProvider) {
        this.classProvider = classProvider;
    }

    @Override
    public ClassNode classNode(Type type) throws NoSuchTypeExists {
        Optional<ClassNode> cl = this.classNodeCache.computeIfAbsent(type, ty -> {
            Optional<byte[]> bytes = this.classProvider.classBytes(ty.getClassName());
            if (!bytes.isPresent()) {
                return Optional.empty();
            }

            ClassNode classNode = new ClassNode(ASM9);
            final ClassReader classReader = new ClassReader(bytes.get());
            classReader.accept(classNode, 0); // TODO: can we skip parsing some parts of a RedirectSet?
            return Optional.of(classNode);
        });

        if (cl.isPresent()) {
            return cl.get();
        }
        throw new NoSuchTypeExists(type);
    }
}
