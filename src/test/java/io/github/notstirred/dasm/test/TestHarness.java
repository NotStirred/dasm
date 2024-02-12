package io.github.notstirred.dasm.test;

import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.transformer.MethodTransform;
import io.github.notstirred.dasm.transformer.Transformer;
import io.github.notstirred.dasm.util.CachingClassProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.ListIterator;
import java.util.Optional;

import static io.github.notstirred.dasm.util.TypeUtil.classNameToDescriptor;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.objectweb.asm.Opcodes.ASM9;

public class TestHarness {
    public static void verifyTransformValid(Class<?> actualClass, Class<?> expectedClass, Class<?> dasmClass) throws DasmWrappedExceptions {
        ClassNode actual = getClassNodeForClass(actualClass);
        ClassNode expected = getClassNodeForClass(expectedClass);
        ClassNode dasm = getClassNodeForClass(dasmClass);

        CachingClassProvider classProvider = new CachingClassProvider(TestHarness::getBytesForClassName);
        Transformer transformer = new Transformer(classProvider, MappingsProvider.IDENTITY);

        AnnotationParser annotationParser = new AnnotationParser(classProvider);

        annotationParser.findRedirectSets(dasm);
        annotationParser.findRedirectSets(actual);
        dasm.name = expected.name;
        Collection<MethodTransform> methodTransforms = annotationParser.buildClassTarget(dasm, "").get().right().get();

        transformer.transform(actual, methodTransforms);

        assertThat(actual).usingRecursiveComparison()
                .withRepresentation(new CustomToString())
                .ignoringFields("name")
                .ignoringFields("innerClasses.name")
                .ignoringFields("innerClasses.innerName")
                .ignoringFieldsMatchingRegexes(".*visited$")
                .ignoringFields("sourceFile")
                .ignoringFieldsOfTypes(LineNumberNode.class)
                .ignoringFieldsOfTypes(LabelNode.class)
                .ignoringFieldsOfTypes(AnnotationNode.class)
                .withEqualsForType((a, b) -> {
                    if (a.size() != b.size()) {
                        return false;
                    }

                    ListIterator<AbstractInsnNode> aIterator = a.iterator();
                    ListIterator<AbstractInsnNode> bIterator = b.iterator();

                    while (aIterator.hasNext()) {
                        AbstractInsnNode aNode = aIterator.next();
                        AbstractInsnNode bNode = bIterator.next();

                        if (aNode instanceof LabelNode && bNode instanceof LabelNode) {
                            return true;
                        }

                        if (!aNode.equals(bNode)) {
                            return false;
                        }
                    }
                    return true;
                }, InsnList.class)
                .withEqualsForType((a, b) -> {
                    if (a.desc.equals(classNameToDescriptor(actual.name)) &&
                            b.desc.equals(classNameToDescriptor(expected.name))) {
                        return true;
                    }
                    return a.desc.equals(b.desc);
                }, TypeInsnNode.class)
                .withEqualsForType((a, b) -> {
                    if (a.desc.equals(classNameToDescriptor(actual.name)) &&
                            b.desc.equals(classNameToDescriptor(expected.name))) {
                        return true;
                    }
                    return a.desc.equals(b.desc);
                }, LocalVariableNode.class)
                .usingOverriddenEquals()
                .isEqualTo(expected);
    }

    private static ClassNode getClassNodeForClass(Class<?> clazz) {
        ClassNode dst = new ClassNode(ASM9);
        final ClassReader classReader = new ClassReader(getBytesForClassName(clazz.getName()).get());
        classReader.accept(dst, 0);
        return dst;
    }

    private static Optional<byte[]> getBytesForClassName(String className) {
        try (InputStream classStream = TestHarness.class.getClassLoader().getResourceAsStream(className.replace(".", "/") + ".class")) {
            byte[] bytes = new byte[classStream.available()];
            if (classStream.read(bytes) == bytes.length) {
                return Optional.of(bytes);
            }
        } catch (IOException ignored) {
        }
        return Optional.empty();
    }
}
