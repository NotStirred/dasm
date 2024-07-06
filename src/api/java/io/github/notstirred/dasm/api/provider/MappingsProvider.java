package io.github.notstirred.dasm.api.provider;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;

public interface MappingsProvider {
    MappingsProvider IDENTITY = new MappingsProvider() {
        @Override
        public String mapFieldName(String owner, String fieldName, String descriptor) {
            return fieldName;
        }

        @Override
        public String mapMethodName(String owner, String methodName, String descriptor) {
            return methodName;
        }

        @Override
        public String mapClassName(String className) {
            return className;
        }
    };

    String mapFieldName(String owner, String fieldName, String descriptor);

    String mapMethodName(String owner, String methodName, String descriptor);

    String mapClassName(String className);

    default Type remapType(Type type) {
        return Method.getMethod(this.mapClassName(type.getClassName()).replace('/', '.') + " x()").getReturnType();
    }

    default Type remapDescType(Type t) {
        if (t.getSort() == ARRAY) {
            int dimCount = t.getDimensions();
            StringBuilder prefix = new StringBuilder(dimCount);
            for (int i = 0; i < dimCount; i++) {
                prefix.append('[');
            }
            return Type.getType(prefix + remapDescType(t.getElementType()).getDescriptor());
        }
        if (t.getSort() != OBJECT) {
            return t;
        }
        String unmapped = t.getClassName();
        if (unmapped.endsWith(";")) {
            unmapped = unmapped.substring(1, unmapped.length() - 1);
        }
        String mapped = this.mapClassName(unmapped);
        String mappedDesc = 'L' + mapped.replace('.', '/') + ';';
        return Type.getType(mappedDesc);
    }
}
