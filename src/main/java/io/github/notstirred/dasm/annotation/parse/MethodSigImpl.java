package io.github.notstirred.dasm.annotation.parse;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MethodSigImpl {
    public static Optional<Method> parse(Map<String, Object> annotation) throws InvalidMethodSignature {
        if (annotation == null) {
            return Optional.empty();
        }

        String value = ((String) annotation.get("value"));
        if (!value.isEmpty()) {
            int parametersStart = value.indexOf('(');
            if (parametersStart == -1) { // did not find
                throw new InvalidMethodSignature(value);
            }
            return Optional.of(new Method(value.substring(0, parametersStart), value.substring(parametersStart)));
        }

        Type ret = (Type) annotation.get("ret");
        @SuppressWarnings("unchecked") List<Type> args = (List<Type>) annotation.get("args");
        String name = (String) annotation.get("value");

        return Optional.of(new Method(name, Type.getMethodDescriptor(ret, args.toArray(new Type[0]))));
    }

    public static class InvalidMethodSignature extends Exception {
        public final String value;

        public InvalidMethodSignature(String value) {
            this.value = value;
        }
    }
}
