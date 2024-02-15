package io.github.notstirred.dasm.util;

public class Util {
    public static boolean atLeastTwo(boolean a, boolean b, boolean c) {
        return a && (b || c) || (b && c);
    }
}
