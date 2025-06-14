package io.github.notstirred.dasm.util;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;

public class ClassNodeUtil {
    /**
     * ASM's {@link ClassNode#outerClass} is only set when the class definition is within a method.
     * If not present this method finds it from the {@link ClassNode#innerClasses} self entry.
     */
    @Nullable
    public static String outerClass(ClassNode node) {
        if (node.outerClass != null) {
            return node.outerClass;
        }

        return node.innerClasses.stream()
                .filter(innerClass -> innerClass.name.equals(node.name)).findFirst()
                .map(innerClass -> innerClass.outerName).orElse(null);
    }
}
