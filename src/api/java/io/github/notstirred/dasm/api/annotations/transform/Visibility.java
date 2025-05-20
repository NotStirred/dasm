package io.github.notstirred.dasm.api.annotations.transform;

import static org.objectweb.asm.Opcodes.*;

public enum Visibility {
    SAME_AS_TARGET(0),
    PUBLIC(ACC_PUBLIC),
    PROTECTED(ACC_PROTECTED),
    PRIVATE(ACC_PRIVATE),
    PACKAGE_PROTECTED(0);

    Visibility(int access) {
        this.access = access;
    }

    public final int access;

    public static Visibility fromAccess(int access) {
        if ((access & ACC_PUBLIC) != 0) {
            return PUBLIC;
        } else if ((access & ACC_PROTECTED) != 0) {
            return PROTECTED;
        } else if ((access & ACC_PRIVATE) != 0) {
            return PRIVATE;
        }
        return PACKAGE_PROTECTED;
    }
}
