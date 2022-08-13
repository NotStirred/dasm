package io.github.opencubicchunks.dasm;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Type.ARRAY;
import static org.objectweb.asm.Type.OBJECT;
import static org.objectweb.asm.Type.getObjectType;
import static org.objectweb.asm.Type.getType;
import static org.objectweb.asm.commons.Method.getMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import com.google.common.collect.Sets;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class Transformer {
    private static final Logger LOGGER = Logger.getLogger(Transformer.class.getName());

    private final MappingsProvider mappingsProvider;
    private final boolean isDev;

    public Transformer(MappingsProvider mappingsProvider, boolean isDev) {
        this.mappingsProvider = mappingsProvider;
        this.isDev = isDev;
    }

    public void transformClass(ClassNode targetClass, RedirectsParser.ClassTarget target, List<RedirectsParser.RedirectSet> redirectSets) {
        Map<Type, Type> typeRedirects = new HashMap<>();
        Map<ClassField, String> fieldRedirects = new HashMap<>();
        Map<ClassMethod, String> methodRedirects = new HashMap<>();

        for (RedirectsParser.RedirectSet redirectSet : redirectSets) {
            for (RedirectsParser.RedirectSet.TypeRedirect typeRedirect : redirectSet.getTypeRedirects()) {
                typeRedirects.put(getObjectType(typeRedirect.srcClassName()), getObjectType(typeRedirect.dstClassName()));
            }

            for (RedirectsParser.RedirectSet.FieldRedirect fieldRedirect : redirectSet.getFieldRedirects()) {
                //annoying syntax only on field
                String desc = fieldRedirect.fieldDesc();
                if (desc.length() > 1) { //not a primitive type
                    desc = "L" + desc;
                }
                desc = desc + ";";
                fieldRedirects.put(new ClassField(fieldRedirect.owner(), fieldRedirect.srcFieldName(), desc), fieldRedirect.dstFieldName());
            }

            for (RedirectsParser.RedirectSet.MethodRedirect methodRedirect : redirectSet.getMethodRedirects()) {
                if (methodRedirect.mappingsOwner() == null) {
                    methodRedirects.put(
                            new ClassMethod(getObjectType(methodRedirect.owner()), getMethod(methodRedirect.returnType() + " " + methodRedirect.srcMethodName())),
                            methodRedirect.dstMethodName()
                    );
                } else {
                    methodRedirects.put(new ClassMethod(getObjectType(methodRedirect.owner()), getMethod(methodRedirect.srcMethodName()), getObjectType(methodRedirect.mappingsOwner())),
                            methodRedirect.dstMethodName()
                    );
                }
            }
        }

        target.getTargetMethods().forEach(targetMethod -> {
            String old = targetMethod.srcMethodName();
            String newName = targetMethod.dstMethodName();
            MethodNode newMethod = cloneAndApplyRedirects(targetClass, new ClassMethod(getObjectType(targetMethod.owner()), getMethod(targetMethod.returnType() + " " + old)), newName,
                    methodRedirects,
                    fieldRedirects,
                    typeRedirects);
            if (targetMethod.makeSyntheticAccessor()) {
                makeStaticSyntheticAccessor(targetClass, newMethod);
            }
        });
    }

    private static void makeStaticSyntheticAccessor(ClassNode node, MethodNode newMethod) {
        Type[] params = Type.getArgumentTypes(newMethod.desc);
        Type[] newParams = new Type[params.length + 1];
        System.arraycopy(params, 0, newParams, 1, params.length);
        newParams[0] = getObjectType(node.name);

        Type returnType = Type.getReturnType(newMethod.desc);
        MethodNode newNode = new MethodNode(newMethod.access | ACC_STATIC | ACC_SYNTHETIC, newMethod.name,
                Type.getMethodDescriptor(returnType, newParams), null, null);

        int j = 0;
        for (Type param : newParams) {
            newNode.instructions.add(new VarInsnNode(param.getOpcode(ILOAD), j));
            j += param.getSize();
        }
        newNode.instructions.add(new MethodInsnNode(INVOKEVIRTUAL, node.name, newMethod.name, newMethod.desc, false));
        newNode.instructions.add(new InsnNode(returnType.getOpcode(IRETURN)));
        node.methods.add(newNode);
    }

    private MethodNode cloneAndApplyRedirects(ClassNode node, ClassMethod existingMethodIn, String newName,
                                                     Map<ClassMethod, String> methodRedirectsIn, Map<ClassField, String> fieldRedirectsIn, Map<Type, Type> typeRedirectsIn) {
        LOGGER.info("Transforming " + node.name + ": Cloning method " + existingMethodIn.method.getName() + " " + existingMethodIn.method.getDescriptor() + " "
                + "into " + newName + " and applying remapping");
        Method existingMethod = remapMethod(existingMethodIn).method;

        MethodNode m = node.methods.stream()
                .filter(x -> existingMethod.getName().equals(x.name) && existingMethod.getDescriptor().equals(x.desc))
                .findAny().orElseThrow(() -> new IllegalStateException("Target method " + existingMethod + " not found"));

        Map<Handle, String> redirectedLambdas = cloneAndApplyLambdaRedirects(node, m, methodRedirectsIn, fieldRedirectsIn, typeRedirectsIn);

        Set<String> defaultKnownClasses = Sets.newHashSet(
                Type.getType(Object.class).getInternalName(),
                Type.getType(String.class).getInternalName(),
                node.name
        );

        Map<String, String> methodRedirects = new HashMap<>();
        for (ClassMethod classMethodUnmapped : methodRedirectsIn.keySet()) {
            ClassMethod classMethod = remapMethod(classMethodUnmapped);
            methodRedirects.put(
                    classMethod.owner.getInternalName() + "." + classMethod.method.getName() + classMethod.method.getDescriptor(),
                    methodRedirectsIn.get(classMethodUnmapped)
            );
        }
        for (Handle handle : redirectedLambdas.keySet()) {
            methodRedirects.put(
                    handle.getOwner() + "." + handle.getName() + handle.getDesc(),
                    redirectedLambdas.get(handle)
            );
        }

        Map<String, String> fieldRedirects = new HashMap<>();
        for (ClassField classFieldUnmapped : fieldRedirectsIn.keySet()) {
            ClassField classField = remapField(classFieldUnmapped);
            fieldRedirects.put(
                    classField.owner.getInternalName() + "." + classField.name,
                    fieldRedirectsIn.get(classFieldUnmapped)
            );
        }

        Map<String, String> typeRedirects = new HashMap<>();
        for (Type type : typeRedirectsIn.keySet()) {
            typeRedirects.put(remapType(type).getInternalName(), remapType(typeRedirectsIn.get(type)).getInternalName());
        }

        methodRedirects.forEach((old, n) -> LOGGER.info("Method mapping: " + old + " -> " + n));
        fieldRedirects.forEach((old, n) -> LOGGER.info("Field mapping: " + old + " -> " + n));
        typeRedirects.forEach((old, n) -> LOGGER.info("Type mapping: " + old + " -> " + n));

        Remapper remapper = new Remapper() {
            @Override
            public String mapMethodName(final String owner, final String name, final String descriptor) {
                if (name.equals("<init>")) {
                    return name;
                }
                String key = owner + '.' + name + descriptor;
                String mappedName = methodRedirects.get(key);
                if (mappedName == null) {
                    if (isDev) {
                        LOGGER.warning("NOTE: handling METHOD redirect to self: " + key);
                    }
                    methodRedirects.put(key, name);
                    return name;
                }
                return mappedName;
            }

            @Override
            public String mapInvokeDynamicMethodName(final String name, final String descriptor) {
                if (isDev) {
                    LOGGER.warning("NOTE: remapping invokedynamic to self: " + name + "." + descriptor);
                }
                return name;
            }

            @Override
            public String mapFieldName(final String owner, final String name, final String descriptor) {
                String key = owner + '.' + name;
                String mapped = fieldRedirects.get(key);
                if (mapped == null) {
                    if (isDev) {
                        LOGGER.warning("NOTE: handling FIELD redirect to self: " + key);
                    }
                    fieldRedirects.put(key, name);
                    return name;
                }
                return mapped;
            }

            @Override
            public String map(final String key) {
                String mapped = typeRedirects.get(key);
                if (mapped == null && defaultKnownClasses.contains(key)) {
                    mapped = key;
                }
                if (mapped == null) {
                    if (isDev) {
                        LOGGER.warning("NOTE: handling CLASS redirect to self: " + key);
                    }
                    typeRedirects.put(key, key);
                    return key;
                }
                return mapped;
            }
        };
        String desc = m.desc;
        Type[] params = Type.getArgumentTypes(desc);
        Type ret = Type.getReturnType(desc);
        for (int i = 0; i < params.length; i++) {
            if (params[i].getSort() == Type.OBJECT) {
                params[i] = getObjectType(remapper.map(params[i].getInternalName()));
            }
        }
        if (ret.getSort() == Type.OBJECT) {
            ret = getObjectType(remapper.map(ret.getInternalName()));
        }
        String mappedDesc = Type.getMethodDescriptor(ret, params);

        MethodNode existingOutput = findExistingMethod(node, newName, mappedDesc);
        MethodNode output;
        if (existingOutput != null) {
            LOGGER.info("Copying code into existing method " + newName + " " + mappedDesc);
            output = existingOutput;
        } else {
            output = new MethodNode(m.access, newName, mappedDesc, null, m.exceptions.toArray(new String[0]));
        }

        MethodVisitor mv = new MethodVisitor(ASM7, output) {
            @Override public void visitLineNumber(int line, Label start) {
                super.visitLineNumber(line + 10000, start);
            }
        };
        MethodRemapper methodRemapper = new MethodRemapper(mv, remapper);

        m.accept(methodRemapper);
        output.name = newName;
        // remove protected and private, add public
        output.access &= ~(ACC_PROTECTED | ACC_PRIVATE);
        output.access |= ACC_PUBLIC;
        node.methods.add(output);

        return output;
    }

    private static MethodNode findExistingMethod(ClassNode node, String name, String desc) {
        return node.methods.stream().filter(m -> m.name.equals(name) && m.desc.equals(desc)).findAny().orElse(null);
    }

    private ClassField remapField(ClassField clField) {
        Type mappedType = remapType(clField.owner);
        String mappedName = this.mappingsProvider.mapFieldName("intermediary",
                clField.owner.getClassName(), clField.name, clField.desc.getDescriptor());
        Type mappedDesc = remapDescType(clField.desc);
        if (clField.name.contains("field") && isDev && mappedName.equals(clField.name)) {
            throw new Error("Fail! Mapping field " + clField.name + " failed in dev!");
        }
        return new ClassField(mappedType, mappedName, mappedDesc);
    }

    @NotNull private ClassMethod remapMethod(ClassMethod clMethod) {
        Type[] params = Type.getArgumentTypes(clMethod.method.getDescriptor());
        Type returnType = Type.getReturnType(clMethod.method.getDescriptor());

        Type mappedType = remapType(clMethod.owner);
        String mappedName = this.mappingsProvider.mapMethodName("intermediary",
                clMethod.mappingOwner.getClassName(), clMethod.method.getName(), clMethod.method.getDescriptor());
        if (clMethod.method.getName().contains("method") && isDev && mappedName.equals(clMethod.method.getName())) {
            throw new Error("Fail! Mapping method " + clMethod.method.getName() + " failed in dev!");
        }
        Type[] mappedParams = new Type[params.length];
        for (int i = 0; i < params.length; i++) {
            mappedParams[i] = remapDescType(params[i]);
        }
        Type mappedReturnType = remapDescType(returnType);
        return new ClassMethod(mappedType, new Method(mappedName, mappedReturnType, mappedParams));
    }

    private Type remapDescType(Type t) {
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
        String mapped = this.mappingsProvider.mapClassName("intermediary", unmapped);
        String mappedDesc = 'L' + mapped.replace('.', '/') + ';';
        if (unmapped.contains("class") && isDev && mapped.equals(unmapped)) {
            throw new Error("Fail! Mapping class " + unmapped + " failed in dev!");
        }
        return Type.getType(mappedDesc);
    }

    private Type remapType(Type t) {
        String unmapped = t.getClassName();
        String mapped = this.mappingsProvider.mapClassName("intermediary", unmapped);
        if (unmapped.contains("class") && isDev && mapped.equals(unmapped)) {
            throw new Error("Fail! Mapping class " + unmapped + " failed in dev!");
        }
        return Type.getObjectType(mapped.replace('.', '/'));
    }

    private Map<Handle, String> cloneAndApplyLambdaRedirects(ClassNode node, MethodNode method, Map<ClassMethod, String> methodRedirectsIn,
                                                             Map<ClassField, String> fieldRedirectsIn, Map<Type, Type> typeRedirectsIn) {
        Map<Handle, String> lambdaRedirects = new HashMap<>();
        for (AbstractInsnNode instruction : method.instructions) {
            if (instruction.getOpcode() == INVOKEDYNAMIC) {
                InvokeDynamicInsnNode invoke = (InvokeDynamicInsnNode) instruction;
                String bootstrapMethodName = invoke.bsm.getName();
                String bootstrapMethodOwner = invoke.bsm.getOwner();
                if (bootstrapMethodName.equals("metafactory") && bootstrapMethodOwner.equals("java/lang/invoke/LambdaMetafactory")) {
                    for (Object bsmArg : invoke.bsmArgs) {
                        if (bsmArg instanceof Handle handle) {
                            String owner = handle.getOwner();
                            if (owner.equals(node.name)) {
                                String newName = "cc$redirect$" + handle.getName();
                                lambdaRedirects.put(handle, newName);
                                cloneAndApplyRedirects(node, new ClassMethod(Type.getObjectType(handle.getOwner()),
                                                new Method(handle.getName(), handle.getDesc())),
                                        newName, methodRedirectsIn, fieldRedirectsIn, typeRedirectsIn);
                            }
                        }
                    }
                }
            }
        }
        return lambdaRedirects;
    }

    private static final class ClassMethod {
        final Type owner;
        final Method method;
        final Type mappingOwner;

        ClassMethod(Type owner, Method method) {
            this.owner = owner;
            this.method = method;
            this.mappingOwner = owner;
        }

        // mapping owner because mappings owner may not be the same as in the call site
        ClassMethod(Type owner, Method method, Type mappingOwner) {
            this.owner = owner;
            this.method = method;
            this.mappingOwner = mappingOwner;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassMethod that = (ClassMethod) o;
            return owner.equals(that.owner) && method.equals(that.method) && mappingOwner.equals(that.mappingOwner);
        }

        @Override public int hashCode() {
            return Objects.hash(owner, method, mappingOwner);
        }

        @Override public String toString() {
            return "ClassMethod{" +
                    "owner=" + owner +
                    ", method=" + method +
                    ", mappingOwner=" + mappingOwner +
                    '}';
        }
    }

    private static final class ClassField {
        final Type owner;
        final String name;
        final Type desc;

        ClassField(String owner, String name, String desc) {
            this.owner = getObjectType(owner);
            this.name = name;
            this.desc = getType(desc);
        }

        ClassField(Type owner, String name, Type desc) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
        }

        @Override public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ClassField that = (ClassField) o;
            return owner.equals(that.owner) && name.equals(that.name) && desc.equals(that.desc);
        }

        @Override public int hashCode() {
            return Objects.hash(owner, name, desc);
        }

        @Override public String toString() {
            return "ClassField{" +
                    "owner=" + owner +
                    ", name='" + name + '\'' +
                    ", desc=" + desc +
                    '}';
        }
    }
}