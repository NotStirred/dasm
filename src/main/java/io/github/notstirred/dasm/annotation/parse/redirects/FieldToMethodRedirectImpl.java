package io.github.notstirred.dasm.annotation.parse.redirects;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.FieldToMethodRedirect;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.data.Field;
import io.github.notstirred.dasm.util.ReferenceUtil;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseOptionalRefAnnotation;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

@Data
public class FieldToMethodRedirectImpl {
    private final ClassField srcField;
    private final ClassMethod getterDstMethod;
    private final Optional<ClassMethod> setterDstMethod;
    private final boolean isStatic;
    private final boolean isDstOwnerInterface;

    public static Optional<FieldToMethodRedirectImpl> parse(Type fieldOwner, boolean dstOwnerIsInterface, MethodNode methodNode,
                                                            Type dstOwner)
            throws RefImpl.RefAnnotationGivenNoArguments, ReferenceUtil.InvalidReference {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, FieldToMethodRedirect.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, FieldToMethodRedirect.class);

        Field srcField = ReferenceUtil.parseFieldReference((String) values.get("value"));

        String setter = (String) values.get("setter");

        Type mappingsOwner = parseOptionalRefAnnotation((AnnotationNode) values.get("mappingsOwner")).orElse(fieldOwner);

        Method getter = new Method(methodNode.name, methodNode.desc);
        return Optional.of(new FieldToMethodRedirectImpl(
                new ClassField(fieldOwner, mappingsOwner, srcField.type(), srcField.name()),
                new ClassMethod(dstOwner, getter),
                setter.isEmpty() ? Optional.empty() : Optional.of(new ClassMethod(dstOwner, new Method(setter, setterDescriptorFor(getter)))),
                (methodNode.access & ACC_STATIC) != 0,
                dstOwnerIsInterface
        ));
    }

    public static @NotNull String setterDescriptorFor(Method getter) {
        return "(" + getter.getReturnType().getDescriptor() + ")V";
    }
}
