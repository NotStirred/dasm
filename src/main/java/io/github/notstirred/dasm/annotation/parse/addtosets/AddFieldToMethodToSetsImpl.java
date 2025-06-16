package io.github.notstirred.dasm.annotation.parse.addtosets;

import io.github.notstirred.dasm.annotation.AnnotationUtil;
import io.github.notstirred.dasm.annotation.parse.FieldSigImpl;
import io.github.notstirred.dasm.annotation.parse.MethodSigImpl;
import io.github.notstirred.dasm.annotation.parse.RefImpl;
import io.github.notstirred.dasm.api.annotations.redirect.redirects.AddFieldToMethodToSets;
import io.github.notstirred.dasm.data.ClassMethod;
import io.github.notstirred.dasm.data.Field;
import lombok.Data;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.github.notstirred.dasm.annotation.parse.RefImpl.parseOptionalRefAnnotation;
import static io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl.setterDescriptorFor;

@Data
public class AddFieldToMethodToSetsImpl {
    private final List<Type> containers;

    private final Field srcField;
    private final Optional<Type> mappingsOwner;

    private final ClassMethod dstMethod;
    private final Optional<ClassMethod> dstSetterMethod;

    private final boolean isStatic;

    public static Optional<AddFieldToMethodToSetsImpl> parse(Type dstOwner, MethodNode methodNode)
            throws RefImpl.RefAnnotationGivenNoArguments, MethodSigImpl.InvalidMethodSignature, MethodSigImpl.EmptySrcName {
        AnnotationNode annotation = AnnotationUtil.getAnnotationIfPresent(methodNode.invisibleAnnotations, AddFieldToMethodToSets.class);
        if (annotation == null) {
            return Optional.empty();
        }

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(annotation, AddFieldToMethodToSets.class);

        Field srcField = FieldSigImpl.parse((AnnotationNode) values.get("field"));

        String setter = (String) values.get("setter");

        Optional<Type> mappingsOwner = parseOptionalRefAnnotation((AnnotationNode) values.get("mappingsOwner"));

        List<Type> sets = (List<Type>) values.get("containers");

        Method getter = new Method(methodNode.name, methodNode.desc);
        return Optional.of(new AddFieldToMethodToSetsImpl(
                sets,
                new Field(srcField.type(), srcField.name()),
                mappingsOwner,
                new ClassMethod(dstOwner, getter),
                setter.isEmpty() ? Optional.empty() : Optional.of(new ClassMethod(dstOwner, new Method(setter, setterDescriptorFor(getter)))),
                (methodNode.access & Opcodes.ACC_STATIC) != 0
        ));
    }
}
