package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.api.annotations.selector.MethodSig;
import io.github.notstirred.dasm.exception.DasmAnnotationException;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class MethodSigImpl {
    public static Method parse(AnnotationNode annotation) throws InvalidMethodSignature, RefImpl.RefAnnotationGivenNoArguments, EmptySrcName {
        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, MethodSig.class);

        String value = ((String) values.get("value"));
        if (!value.isEmpty()) {
            int parametersStart = value.indexOf('(');
            if (parametersStart == -1) { // did not find
                throw new InvalidMethodSignature(value);
            }
            return new Method(value.substring(0, parametersStart), value.substring(parametersStart));
        }

        Type ret = RefImpl.parseRefAnnotation("ret", values);
        @SuppressWarnings("unchecked") List<AnnotationNode> argAnnotations = ((List<AnnotationNode>) values.get("args"));
        List<Type> args = new ArrayList<>();
        for (AnnotationNode argAnnotation : argAnnotations) {
            args.add(RefImpl.parseUnnamedRefAnnotation(argAnnotation));
        }

        String name = (String) values.get("name");
        if (name.isEmpty()) {
            throw new EmptySrcName();
        }

        return new Method(name, Type.getMethodDescriptor(ret, args.toArray(new Type[0])));
    }

    public static class InvalidMethodSignature extends DasmAnnotationException {
        public InvalidMethodSignature(String value) {
            super("Invalid method signature: `" + value + "`");
        }
    }

    public static class EmptySrcName extends DasmAnnotationException {
        public EmptySrcName() {
            super("MethodSig has empty name");
        }
    }
}
