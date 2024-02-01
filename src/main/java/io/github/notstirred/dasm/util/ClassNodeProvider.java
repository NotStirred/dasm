package io.github.notstirred.dasm.util;

import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

public interface ClassNodeProvider {
    /**
     * @param type The type to get the {@link ClassNode} for.
     * @return The {@link ClassNode}, it should <b>never</b> be modified.
     */
    ClassNode classNode(Type type) throws NoSuchTypeExists;
}
