package io.github.notstirred.dasm.test.tests.integration.add_parameter_multiple_static;

public class AddParameterMultipleStaticInput {
    public static Object[] method1static(Object a) {
        Object[] out = new Object[5];
        for (int i = 0; i < 5; i++) {
            out[i] = new Object();
        }
        return out;
    }

    public static String[] method2static(String a, String b, String c) {
        String[] out = new String[5];
        for (int i = 0; i < 5; i++) {
            out[i] = new String();
        }
        return out;
    }

    public static String[] method3static(long a, long b) {
        String[] out = new String[5];
        double m = 0;
        for (long i = 0; i < 5; i++) {
            out[(int) i] = new String();
        }
        return out;
    }
}