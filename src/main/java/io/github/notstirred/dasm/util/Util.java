package io.github.notstirred.dasm.util;

public class Util {
    public static boolean atLeastTwoOf(boolean a, boolean b, boolean c) {
        return a && (b || c) || (b && c);
    }
}
