package io.github.notstirred.dasm.test;

import com.google.common.collect.Lists;
import io.github.notstirred.dasm.annotation.AnnotationParser;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.DasmContext;
import io.github.notstirred.dasm.exception.DasmException;
import io.github.notstirred.dasm.notify.Notification;
import io.github.notstirred.dasm.test.targets.*;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Bar;
import io.github.notstirred.dasm.test.targets.inherited_transforms.Foo;
import io.github.notstirred.dasm.test.utils.ByteArrayClassLoader;
import io.github.notstirred.dasm.transformer.Transformer;
import io.github.notstirred.dasm.transformer.data.MethodTransform;
import io.github.notstirred.dasm.util.CachingClassProvider;
import io.github.notstirred.dasm.util.Pair;
import org.assertj.core.api.RecursiveComparisonAssert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.github.notstirred.dasm.util.TypeUtil.classNameToInternalName;
import static io.github.notstirred.dasm.util.TypeUtil.typeNameToDescriptor;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.objectweb.asm.Opcodes.ASM9;

public class TestHarness {
    private static final Path DASM_OUT = Path.of(".dasm.out");

    /**
     * Verifies that the actualClass equals the expectedClass after transforms in dasmClass+actualClass have been applied
     */
    public static void verifyMethodTransformsValid(Class<?> actualClass, Class<?> expectedClass, Class<?> dasmClass) {
        verifyTransformValid(actualClass, expectedClass, dasmClass, DASM_OUT.resolve("method_transforms"),
                (context, classNode) -> context.buildMethodTargets(classNode, ""),
                Transformer::transform
        );
    }

    /**
     * Verifies that the actualClass equals the expectedClass after transforms in dasmClass+actualClass have been applied
     */
    public static void verifyClassTransformValid(Class<?> actualClass, Class<?> expectedClass, Class<?> dasmClass) {
        verifyTransformValid(actualClass, expectedClass, dasmClass, DASM_OUT.resolve("class_transforms"),
                DasmContext::buildClassTarget,
                Transformer::transform
        );
    }

    @FunctionalInterface
    interface BuildTargets<T> {
        Pair<Optional<T>, List<Notification>> buildTargets(DasmContext context, ClassNode classNode) throws DasmException;
    }

    @FunctionalInterface
    interface DoTransform<T> {
        void doTransform(Transformer transformer, ClassNode classNode, T transforms) throws DasmException;
    }

    /**
     * Verifies that the actualClass equals the expectedClass after transforms in dasmClass+actualClass have been applied
     */
    private static <T> void verifyTransformValid(Class<?> actualClass, Class<?> expectedClass, Class<?> dasmClass, Path basePath, BuildTargets<T> buildTargets, DoTransform<T> doTransform) {
        ClassNode actual = classNodeFromClass(actualClass);
        ClassNode expected = classNodeFromClass(expectedClass);
        ClassNode dasm = classNodeFromClass(dasmClass);

        CachingClassProvider classProvider = new CachingClassProvider(TestHarness::getBytesForClassName);
        Transformer transformer = new Transformer(classProvider, MappingsProvider.IDENTITY);

        AnnotationParser annotationParser = new AnnotationParser(classProvider);

        try {
            Pair<DasmContext, List<Notification>> dasmContextListPair = annotationParser.parseDasmClassNodes(Lists.newArrayList(dasm, actual));
            DasmContext context;
            if (dasmContextListPair.first() != null) {
                context = dasmContextListPair.first();
            } else {
                DasmException dasmException = new DasmException("");
                dasmContextListPair.second().forEach(notification -> {
                    dasmException.addSuppressed(new Exception(notification.message));
                });
                throw dasmException;
            }

            dasm.name = actual.name;
            Pair<Optional<T>, List<Notification>> targetsAndNotifications = buildTargets.buildTargets(context, dasm);
            if (targetsAndNotifications.first().isPresent()) {
                T transforms = targetsAndNotifications.first().get();

                doTransform.doTransform(transformer, actual, transforms);
            } else {
                DasmException dasmException = new DasmException("");
                targetsAndNotifications.second().forEach(notification -> {
                    dasmException.addSuppressed(new Exception(notification.message));
                });
                throw dasmException;
            }
        } catch (DasmException e) {
            throw new Error("Dasm Exception in testing", e);
        }

        // Write and re-read class bytes to fix issues with label nodes being wonky
        byte[] bytecode = classNodeToBytes(actual);
        ClassNode reparsedClassNode = classNodeFromBytes(bytecode);

        try {
            Path path = basePath.resolve(reparsedClassNode.name.replace('.', '/') + ".class").toAbsolutePath();
            Path actualPath = path.getParent().resolve("ACTUAL").resolve(path.getFileName());
            createDirectoriesIfNotExists(actualPath.getParent());
            Files.write(actualPath, bytecode);
            Path expectedPath = path.getParent().resolve("EXPECTED").resolve(expectedClass.getSimpleName() + ".class");
            createDirectoriesIfNotExists(expectedPath.getParent());
            Files.write(expectedPath, classNodeToBytes(expected));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        expected.methods.sort(Comparator.comparing(methodNode -> methodNode.name));
        reparsedClassNode.methods.sort(Comparator.comparing(methodNode -> methodNode.name));

        assertClassNodesEqual(reparsedClassNode, expected);
        callAllMethodsWithDummies(actualClass, expectedClass, reparsedClassNode);
    }

    private static void createDirectoriesIfNotExists(Path path) throws IOException {
        try {
            Files.createDirectories(path);
        } catch (FileAlreadyExistsException e) {
            // ignored
        }
    }

    public static Pair<Optional<Collection<MethodTransform>>, List<Notification>> getRedirectsFor(Class<?> dasmClass, Class<?>... extraDasmClasses) {
        CachingClassProvider classProvider = new CachingClassProvider(TestHarness::getBytesForClassName);
        AnnotationParser annotationParser = new AnnotationParser(classProvider);
        ClassNode dasm = classNodeFromClass(dasmClass);

        List<ClassNode> dasmClasses = new ArrayList<>();
        dasmClasses.add(dasm);
        for (Class<?> clazz : extraDasmClasses) {
            dasmClasses.add(classNodeFromClass(clazz));
        }

        Pair<DasmContext, List<Notification>> dasmContextListPair = annotationParser.parseDasmClassNodes(dasmClasses);
        if (dasmContextListPair.first() == null) {
            return new Pair<>(Optional.empty(), dasmContextListPair.second());
        }
        return dasmContextListPair.first().buildMethodTargets(dasm, "");
    }

    private static byte[] classNodeToBytes(ClassNode actual) {
        ClassWriter classWriter = new ClassWriter(0);
        actual.accept(classWriter);
        byte[] bytecode = classWriter.toByteArray();
        return bytecode;
    }

    private static void assertClassNodesEqual(ClassNode actual, ClassNode expected) {
        configuredAssertThat(actual).withEqualsForType((a, b) -> {
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
                        if ((aNode instanceof FieldInsnNode aField && bNode instanceof FieldInsnNode bField)) {
                            if (!(aField.owner.equals(classNameToInternalName(actual.name)) && bField.owner.equals(classNameToInternalName(expected.name)))) {
                                assertThat(aField.owner).isEqualTo(bField.owner);
                            }
                            assertThat(aField.desc).isEqualTo(bField.desc);
                            assertThat(aField.name).isEqualTo(bField.name);
                            continue;
                        }

                        configuredAssertThat(aNode)
                                .ignoringFields("previousInsn")
                                .ignoringFields("nextInsn")
                                .isEqualTo(bNode);
                    }
                    return true;
                }, InsnList.class)
                .withEqualsForType((a, b) -> {
                    if (a.desc.equals(typeNameToDescriptor(actual.name)) &&
                            b.desc.equals(typeNameToDescriptor(expected.name))) {
                        return true;
                    }
                    return a.desc.equals(b.desc);
                }, TypeInsnNode.class)
                .withEqualsForType((a, b) -> {
                    if (a.desc.equals(typeNameToDescriptor(actual.name)) &&
                            b.desc.equals(typeNameToDescriptor(expected.name))) {
                        return true;
                    }
                    return a.desc.equals(b.desc);
                }, LocalVariableNode.class)
                .isEqualTo(expected);
    }

    private static RecursiveComparisonAssert<?> configuredAssertThat(Object actual) {
        return assertThat(actual).usingRecursiveComparison()
                .withRepresentation(new CustomToString())
                .ignoringFields("name")
                .ignoringCollectionOrderInFields("methods.localVariables")
                .ignoringFields("innerClasses.name")
                .ignoringFields("innerClasses.innerName")
                .ignoringFields("innerClasses.innerName")
                .ignoringFields("methods.maxStack") // constructorToMethod redirects don't adjust the max stack variable, so we just ignore it
                .ignoringFields("methods.signature") // dasm doesn't attempt to create a valid signature.
                .ignoringFieldsMatchingRegexes(".*visited$")
                .ignoringFields("sourceFile")
                .ignoringFieldsMatchingRegexes(".*line$")
                .ignoringFields("label")
                .ignoringFields("bsmArgs.name")
                .ignoringFields("bsmArgs.owner")
                .ignoringFields("methods.name")
                .ignoringAllOverriddenEquals();
    }

    private static String getUnusedClassName(String originalClassName) {
        int idx = 1;
        String uniqueName = originalClassName + "_" + idx;
        while (CLASS_BYTES.containsKey(uniqueName.replace('/', '.'))) {
            idx++;
            uniqueName = originalClassName + "_" + idx;
        }
        return uniqueName;
    }

    private static void callAllMethodsWithDummies(Class<?> actualClass, Class<?> expectedClass, ClassNode actual) {
        // Modify the name, as the input class is already loaded by this point.
        ClassNode renamedActual = new ClassNode(ASM9);
        actual.accept(new ClassRemapper(renamedActual, new SimpleRemapper(actual.name, getUnusedClassName(actual.name))));

        String transformedName = renamedActual.name.replace('/', '.');

        // Write transformed class
        ClassWriter classWriter = new ClassWriter(0);
        renamedActual.accept(classWriter);

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
            // Can't test abstract classes
            if (actualClassLoaded.accessFlags().contains(AccessFlag.ABSTRACT)) {
                return;
            }
            actualInstance = actualClassLoaded.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Failed to create transformed class instance for " + actualClassLoaded.getName(), e);
        }
        Object expectedInstance;
        try {
            expectedInstance = expectedClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException("Failed to create transformed class instance for " + actualClassLoaded.getName(), e);
        }

        for (Method expectedMethod : expectedClass.getDeclaredMethods()) {
            if (expectedMethod.getName().contains("$jacocoInit"))
                continue;

            expectedMethod.setAccessible(true);

            Method actualMethod;
            try {
                actualMethod = actualClassLoaded.getDeclaredMethod(expectedMethod.getName(), expectedMethod.getParameterTypes());
                actualMethod.setAccessible(true);
                // Can't test abstract methods
                if (actualMethod.accessFlags().contains(AccessFlag.ABSTRACT)) {
                    continue;
                }
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

    private static ClassNode classNodeFromClass(Class<?> clazz) {
        ClassNode dst = new ClassNode(ASM9);
        final ClassReader classReader = new ClassReader(getBytesForClassName(clazz.getName()).get());
        classReader.accept(dst, 0);
        return dst;
    }

    private static ClassNode classNodeFromBytes(byte[] bytecode) {
        ClassNode dst = new ClassNode(ASM9);
        final ClassReader classReader = new ClassReader(bytecode);
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
        DUMMY_VALUES.put(int[].class, new int[]{1, 1, 1});
        DUMMY_VALUES.put(Integer.class, Integer.valueOf(2));
        DUMMY_VALUES.put(float.class, 3.0f);
        DUMMY_VALUES.put(float[].class, new float[]{3.0f, 3.0f, 3.0f});
        DUMMY_VALUES.put(float[][].class, new float[][]{new float[]{3.0f, 3.0f, 3.0f}});
        DUMMY_VALUES.put(Float.class, Float.valueOf(4.0f));
        DUMMY_VALUES.put(boolean.class, true);
        DUMMY_VALUES.put(boolean[].class, new boolean[]{true, true, true});
        DUMMY_VALUES.put(Boolean.class, Boolean.valueOf(false));
        DUMMY_VALUES.put(long.class, 7L);
        DUMMY_VALUES.put(long[].class, new long[]{7L, 7L, 7L});
        DUMMY_VALUES.put(Long.class, Long.valueOf(8));
        DUMMY_VALUES.put(double.class, 9.0);
        DUMMY_VALUES.put(double[].class, new double[]{9.0, 9.0, 9.0});
        DUMMY_VALUES.put(Double.class, Double.valueOf(10.0));
        DUMMY_VALUES.put(byte.class, (byte) 11);
        DUMMY_VALUES.put(byte[].class, new byte[]{(byte) 11, (byte) 11, (byte) 11});
        DUMMY_VALUES.put(Byte.class, Byte.valueOf((byte) 12));
        DUMMY_VALUES.put(short.class, (short) 13);
        DUMMY_VALUES.put(short[].class, new short[]{(short) 13, (short) 13, (short) 13});
        DUMMY_VALUES.put(Short.class, Short.valueOf((short) 14));
        DUMMY_VALUES.put(Object.class, new Object());
        DUMMY_VALUES.put(Object[].class, new Object[]{new Object(), new Object(), new Object()});
        DUMMY_VALUES.put(String.class, "a");
        DUMMY_VALUES.put(String[].class, new String[]{"b", "c", "d"});
        DUMMY_VALUES.put(Vec3i.class, new Vec3i(5, -15, 190));
        DUMMY_VALUES.put(CubePos.class, new CubePos(-9, 3, 128736));
        DUMMY_VALUES.put(CubePosInterface.class, new CubePos(13, 341, -76));
        DUMMY_VALUES.put(A.class, new A());
        DUMMY_VALUES.put(B.class, new B());
        DUMMY_VALUES.put(C.class, new C());
        DUMMY_VALUES.put(Bar.class, new Bar());
        DUMMY_VALUES.put(Foo.class, new Foo());
    }
}
