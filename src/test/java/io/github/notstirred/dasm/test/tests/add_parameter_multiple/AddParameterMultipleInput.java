package io.github.notstirred.dasm.test.tests.add_parameter_multiple;

public class AddParameterMultipleInput {
    public Object[] method1(Object a) {
        Object[] out = new Object[5];
        for (int i = 0; i < 5; i++) {
            out[i] = new Object();
        }
        return out;
    }

    public String[] method2(String a, String b, String c) {
        String[] out = new String[5];
        for (int i = 0; i < 5; i++) {
            out[i] = new String();
        }
        return out;
    }

    public String[] method3(long a, long b) {
        String[] out = new String[5];
        double m = 0;
        for (long i = 0; i < 5; i++) {
            out[(int) i] = new String();
        }
        return out;
    }
}