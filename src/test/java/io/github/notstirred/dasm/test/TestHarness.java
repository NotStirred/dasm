package io.github.notstirred.dasm.test;

import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.exception.wrapped.DasmWrappedExceptions;
import io.github.notstirred.dasm.test.utils.ByteArrayClassLoader;
import io.github.notstirred.dasm.transformer.Transformer;
import io.github.notstirred.dasm.transformer.data.ClassTransform;
import io.github.notstirred.dasm.transformer.data.MethodTransform;
import io.github.notstirred.dasm.util.CachingClassProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static io.github.notstirred.dasm.util.TypeUtil.classNameToDescriptor;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.objectweb.asm.Opcodes.ASM9;

public class TestHarness {
    public static void verifyMethodTransformsValid(Class<?> actualClass, Class<?> expectedClass, Class<?> dasmClass) {
        verifyMethodTransformsValid(actualClass, expectedClass, dasmClass, 0);
    }

    /**
     * Prefer {@link #verifyMethodTransformsValid(Class, Class, Class)} if only one test uses the input class
     * Verifies that the actualClass equals the expectedClass after transforms in dasmClass+actualClass have been applied
     *
     * @param subTestIdx Which subtest this is. Use when multiple tests use the same input actualClass. A different index for each expectedClass should be used.
     */
    public static void verifyMethodTransformsValid(Class<?> actualClass, Class<?> expectedClass, Class<?> dasmClass, int subTestIdx) {
        ClassNode actual = getClassNodeForClass(actualClass);
        ClassNode expected = getClassNodeForClass(expectedClass);
        ClassNode dasm = getClassNodeForClass(dasmClass);

        CachingClassProvider classProvider = new CachingClassProvider(TestHarness::getBytesForClassName);
        Transformer transformer = new Transformer(classProvider, MappingsProvider.IDENTITY);

        AnnotationParser annotationParser = new AnnotationParser(classProvider);

        try {
            annotationParser.findRedirectSets(dasm);
            annotationParser.findRedirectSets(actual);
            dasm.name = expected.name;
            Collection<MethodTransform> methodTransforms = annotationParser.buildMethodTargets(dasm, "").get();

            transformer.transform(actual, methodTransforms);
        } catch (DasmWrappedExceptions e) {
            e.printStackTrace();
            throw new Error(e);
        }

        assertClassNodesEqual(actual, expected);

        callAllMethodsWithDummies(actualClass, expectedClass, actual, subTestIdx);
    }

    public static void verifyClassTransformValid(Class<?> actualClass, Class<?> expectedClass, Class<?> dasmClass) {
        verifyClassTransformValid(actualClass, expectedClass, dasmClass, 0);
    }

    /**
     * Prefer {@link #verifyClassTransformValid(Class, Class, Class)} if only one test uses the input class
     * Verifies that the actualClass equals the expectedClass after transforms in dasmClass+actualClass have been applied
     *
     * @param subTestIdx Which subtest this is. Use when multiple tests use the same input actualClass
     */
    public static void verifyClassTransformValid(Class<?> actualClass, Class<?> expectedClass, Class<?> dasmClass, int subTestIdx) {
        ClassNode actual = getClassNodeForClass(actualClass);
        ClassNode expected = getClassNodeForClass(expectedClass);
        ClassNode dasm = getClassNodeForClass(dasmClass);

        CachingClassProvider classProvider = new CachingClassProvider(TestHarness::getBytesForClassName);
        Transformer transformer = new Transformer(classProvider, MappingsProvider.IDENTITY);

        AnnotationParser annotationParser = new AnnotationParser(classProvider);

        try {
            annotationParser.findRedirectSets(dasm);
            annotationParser.findRedirectSets(actual);
            dasm.name = expected.name;
            ClassTransform methodTransforms = annotationParser.buildClassTarget(dasm).get();

            transformer.transform(actual, methodTransforms);
        } catch (DasmWrappedExceptions | NoSuchTypeExists e) {
            e.printStackTrace();
            throw new Error(e);
        }

        assertClassNodesEqual(actual, expected);

        callAllMethodsWithDummies(actualClass, expectedClass, actual, subTestIdx);
    }

    private static void assertClassNodesEqual(ClassNode actual, ClassNode expected) {
        assertThat(actual).usingRecursiveComparison()
                .withRepresentation(new CustomToString())
                .ignoringFields("name")
                .ignoringFields("innerClasses.name")
                .ignoringFields("innerClasses.innerName")
                .ignoringFields("methods.maxStack") // constructorToMethod redirects don't adjust the max stack variable, so we just ignore it
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

                        if ((aNode instanceof LabelNode && bNode instanceof LabelNode) ||
                                (aNode instanceof LineNumberNode && bNode instanceof LineNumberNode)) {
                            continue;
                        }

                        assertThat(aNode).usingRecursiveComparison()
                                .ignoringFields("previousInsn")
                                .ignoringFields("nextInsn")
                                .ignoringFields("line")
                                .isEqualTo(bNode);
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

    private static void callAllMethodsWithDummies(Class<?> actualClass, Class<?> expectedClass, ClassNode actual, int subTestIdx) {
        // Modify the name, as the input class is already loaded by this point.
        actual.name = actual.name + "_TRANSFORMED" + subTestIdx;
        String transformedName = actual.name.replace('/', '.');

        // Write transformed class
        ClassWriter classWriter = new ClassWriter(0);
        actual.accept(classWriter);

        // Load transformed class
        byte[] previouslyLoadedClass = CLASS_BYTES.put(transformedName, classWriter.toByteArray());
        if (previouslyLoadedClass != null) {
            throw new RuntimeException("Two classes with the same name loaded: " + actualClass.getName());
        }

        Class<?> actualClassLoaded;
        try {
            actualClassLoaded = CLASS_LOADER.loadClass(transformedName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not find transformed class to load " + actualClass.getName(), e);
        }

        // Create instances of test classes
        Object actualInstance;
        try {
            actualInstance = actualClassLoaded.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to create transformed class instance for " + actualClassLoaded.getName(), e);
        }
        Object expectedInstance;
        try {
            expectedInstance = expectedClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException("Failed to create transformed class instance for " + actualClassLoaded.getName(), e);
        }

        for (Method expectedMethod : expectedClass.getDeclaredMethods()) {
            expectedMethod.setAccessible(true);

            Method actualMethod;
            try {
                actualMethod = actualClassLoaded.getDeclaredMethod(expectedMethod.getName(), expectedMethod.getParameterTypes());
                actualMethod.setAccessible(true);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Transformed class missing expected method `" + expectedMethod + "`", e);
            }
            // Collect dummy objects for parameter types
            List<Object> params = Arrays.stream(actualMethod.getParameterTypes()).map(clazz -> {
                Object dummy = DUMMY_VALUES.get(clazz);
                if (dummy == null) {
                    throw new RuntimeException("No dummy value for type " + clazz.getName());
                }
                return dummy;
            }).toList();

            // Call transformed method
            Object actualReturnValue;
            try {
                System.out.println("Invoking method `" + actualMethod + "`");
                actualReturnValue = actualMethod.invoke(actualInstance, params.toArray());
            } catch (Throwable t) {
                throw new RuntimeException("Failed to invoke method `" + actualMethod + "` on transformed class " + actualClassLoaded.getName(), t);
            }

            // Call expect method
            Object expectedReturnValue;
            try {
                System.out.println("Invoking method `" + expectedMethod + "`");
                expectedReturnValue = expectedMethod.invoke(expectedInstance, params.toArray());
            } catch (Throwable t) {
                throw new RuntimeException("Failed to invoke method `" + expectedMethod + "` on expected class " + expectedClass.getName(), t);
            }

            // Assert return value equality
            boolean typeOverridesEquals;
            try {
                typeOverridesEquals = !actualClassLoaded.getMethod("equals", Object.class).getDeclaringClass().equals(Object.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(actualClassLoaded.getName() + " doesn't implement equals()?!?!?!");
            }

            // If the type overrides equals, we use isEqualTo(), otherwise compare classes to avoid reference equality
            if (typeOverridesEquals || expectedReturnValue == null || actualReturnValue == null) {
                assertThat(actualReturnValue).isEqualTo(expectedReturnValue);
            } else {
                assertThat(actualReturnValue.getClass()).isEqualTo(expectedReturnValue.getClass());
            }
        }
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

    private static final Map<Class<?>, Object> DUMMY_VALUES = new HashMap<>();
    private static final Map<String, byte[]> CLASS_BYTES = new HashMap<>();
    private static final ClassLoader CLASS_LOADER = new ByteArrayClassLoader(TestHarness.class.getClassLoader(), CLASS_BYTES::get);

    static {
        DUMMY_VALUES.put(int.class, 1);
        DUMMY_VALUES.put(int[].class, new int[]{ 1, 1, 1 });
        DUMMY_VALUES.put(Integer.class, Integer.valueOf(2));
        DUMMY_VALUES.put(float.class, 3.0f);
        DUMMY_VALUES.put(float[].class, new float[]{ 3.0f, 3.0f, 3.0f });
        DUMMY_VALUES.put(Float.class, Float.valueOf(4.0f));
        DUMMY_VALUES.put(boolean.class, true);
        DUMMY_VALUES.put(boolean[].class, new boolean[]{ true, true, true });
        DUMMY_VALUES.put(Boolean.class, Boolean.valueOf(false));
        DUMMY_VALUES.put(long.class, 7L);
        DUMMY_VALUES.put(long[].class, new long[]{ 7L, 7L, 7L });
        DUMMY_VALUES.put(Long.class, Long.valueOf(8));
        DUMMY_VALUES.put(double.class, 9.0);
        DUMMY_VALUES.put(double[].class, new double[]{ 9.0, 9.0, 9.0 });
        DUMMY_VALUES.put(Double.class, Double.valueOf(10.0));
        DUMMY_VALUES.put(byte.class, (byte) 11);
        DUMMY_VALUES.put(byte[].class, new byte[]{ (byte) 11, (byte) 11, (byte) 11 });
        DUMMY_VALUES.put(Byte.class, Byte.valueOf((byte) 12));
        DUMMY_VALUES.put(short.class, (short) 13);
        DUMMY_VALUES.put(short[].class, new short[]{ (short) 13, (short) 13, (short) 13 });
        DUMMY_VALUES.put(Short.class, Short.valueOf((short) 14));
        DUMMY_VALUES.put(Object.class, new Object());
        DUMMY_VALUES.put(Object[].class, new Object[]{ new Object(), new Object(), new Object() });
        DUMMY_VALUES.put(String.class, "a");
        DUMMY_VALUES.put(String[].class, new String[]{ "b", "c", "d" });
    }
}
