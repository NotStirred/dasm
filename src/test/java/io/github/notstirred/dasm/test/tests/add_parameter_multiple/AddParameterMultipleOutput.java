package io.github.notstirred.dasm.test.tests.add_parameter_multiple;

public class AddParameterMultipleOutput {
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

    public String[] method1out1(float[][] c, String a, float[][] b) {
        String[] out = new String[5];
        for (int i = 0; i < 5; i++) {
            out[i] = new String();
        }
        return out;
    }

    public String[] method1out2(String a, float[][] b, int c, int d) {
        String[] out = new String[5];
        for (int i = 0; i < 5; i++) {
            out[i] = new String();
        }
        return out;
    }

    public String[] method2out1(float a, float b, String c, int d, double e, String f, long g, String h, boolean i) {
        String[] out = new String[5];
        for (int j = 0; j < 5; j++) {
            out[j] = new String();
        }
        return out;
    }

    public String[] method3out1(long c, int d, long a, double e, long b, int f, long g) {
        String[] out = new String[5];
        double m = 0;
        for (long i = 0; i < 5; i++) {
            out[(int) i] = new String();
        }
        return out;
    }
}