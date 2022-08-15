package io.github.opencubicchunks.dasm;

import java.util.*;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedirectsParser {
    private static final String TARGET_METHODS_NAME = "targetMethods";
    private static final String USE_SETS_NAME = "useSets";
    private static final String TYPE_REDIRECTS_NAME = "typeRedirects";
    private static final String METHOD_REDIRECTS_NAME = "methodRedirects";
    private static final String FIELD_REDIRECTS_NAME = "fieldRedirects";

    private static final String MAKE_SYNTHETIC_ACCESSOR_NAME = "makeSyntheticAccessor";
    private static final String SHOULD_CLONE_NAME = "shouldClone";
    private static final String MAPPINGS_OWNER_NAME = "mappingsOwner";
    private static final String DST_NAME = "newName";

    public List<RedirectSet> parseRedirectSet(JsonObject json) throws RedirectsParseException {
        List<RedirectSet> redirectSets = new ArrayList<>();
        for (Map.Entry<String, JsonElement> classRedirectObject : json.entrySet()) {
            String redirectSetName = throwOnLengthZero(classRedirectObject.getKey(), () -> "Redirect Set node has empty name");

            JsonElement redirectElement = classRedirectObject.getValue();
            if (!redirectElement.isJsonObject()) {
                throw new RedirectsParseException(String.format("Invalid redirect set node %s", redirectElement));
            }
            JsonObject redirectJson = redirectElement.getAsJsonObject();

            RedirectSet redirectSet = new RedirectSet(redirectSetName);

            if (!redirectJson.has(TYPE_REDIRECTS_NAME) || !redirectJson.get(TYPE_REDIRECTS_NAME).isJsonObject()) {
                throw new RedirectsParseException(String.format("Redirect set has no \"%s\" object", TYPE_REDIRECTS_NAME));
            }
            Set<Map.Entry<String, JsonElement>> typeRedirects = redirectJson.get(TYPE_REDIRECTS_NAME).getAsJsonObject().entrySet();
            parseTypeRedirects(redirectSet, typeRedirects);

            if (!redirectJson.has(FIELD_REDIRECTS_NAME) || !redirectJson.get(FIELD_REDIRECTS_NAME).isJsonObject()) {
                throw new RedirectsParseException(String.format("Redirect set has no \"%s\" object", FIELD_REDIRECTS_NAME));
            }
            Set<Map.Entry<String, JsonElement>> fieldRedirects = redirectJson.get(FIELD_REDIRECTS_NAME).getAsJsonObject().entrySet();
            parseFieldRedirects(redirectSet, fieldRedirects);

            if (!redirectJson.has(METHOD_REDIRECTS_NAME) || !redirectJson.get(METHOD_REDIRECTS_NAME).isJsonObject()) {
                throw new RedirectsParseException(String.format("Redirect set has no \"%s\" object", METHOD_REDIRECTS_NAME));
            }
            Set<Map.Entry<String, JsonElement>> methodRedirects = redirectJson.get(METHOD_REDIRECTS_NAME).getAsJsonObject().entrySet();
            parseMethodRedirects(redirectSet, methodRedirects);

            redirectSets.add(redirectSet);
        }

        return redirectSets;
    }

    public List<ClassTarget> parseClassTargets(JsonObject json) throws RedirectsParseException {
        List<ClassTarget> classTargets = new ArrayList<>();
        for (Map.Entry<String, JsonElement> classRedirectObject : json.entrySet()) {
            String classTargetName = throwOnLengthZero(classRedirectObject.getKey(), () -> "Class target node has empty name");

            JsonElement classTargetElement = classRedirectObject.getValue();
            if (!classTargetElement.isJsonObject()) {
                throw new RedirectsParseException(String.format("Invalid Class target node %s", classTargetElement));
            }
            JsonObject classTargetNode = classTargetElement.getAsJsonObject();

            if (!classTargetNode.has(USE_SETS_NAME)) {
                throw new RedirectsParseException(String.format("Class target has no \"%s\" object", USE_SETS_NAME));
            }

            ClassTarget classTarget = new ClassTarget(classTargetName);

            JsonElement usesSetNameElement = classTargetNode.get(USE_SETS_NAME);
            if (usesSetNameElement.isJsonPrimitive() && usesSetNameElement.getAsJsonPrimitive().isString()) {
                //single set
                classTarget.addUsesSet(throwOnLengthZero(usesSetNameElement.getAsString(), () -> "Specified Class set name has zero length"));
            } else if (usesSetNameElement.isJsonArray()) {
                //multiple sets
                JsonArray usesSetNames = usesSetNameElement.getAsJsonArray();
                for (JsonElement usesSetName : usesSetNames) {
                    classTarget.addUsesSet(usesSetName.getAsString());
                }
            }

            // it's possible the class target to target the whole class, and so has no target methods
            if (classTargetNode.has(TARGET_METHODS_NAME) && classTargetNode.get(TARGET_METHODS_NAME).isJsonObject()) {
                Set<Map.Entry<String, JsonElement>> targetMethodsNode = classTargetNode.get(TARGET_METHODS_NAME).getAsJsonObject().entrySet();
                parseTargetMethods(classTarget, targetMethodsNode);
            } else {
                classTarget.targetWholeClass();
            }

            classTargets.add(classTarget);
        }

        return classTargets;
    }

    private void parseTargetMethods(ClassTarget output, Set<Map.Entry<String, JsonElement>> methodRedirects) throws RedirectsParseException {
        for (Map.Entry<String, JsonElement> methodRedirect : methodRedirects) {
            ImmutableTriple<String, String, String> ownerIdentType = parseSignature(methodRedirect.getKey());
            String ownerName = ownerIdentType.left;
            String srcMethodName = ownerIdentType.middle;
            String returnType = ownerIdentType.right;

            JsonElement value = methodRedirect.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String dstMethodName = throwOnLengthZero(value.getAsString(), () -> String.format("Target method has zero length value for key %s", methodRedirect));
                output.addTarget(new ClassTarget.TargetMethod(ownerName, returnType, null, srcMethodName, dstMethodName, true, false));
            } else if (value.isJsonObject()) { // target method might want a synthetic accessor
                JsonObject targetMethodValue = value.getAsJsonObject();

                if (!targetMethodValue.has(DST_NAME)) {
                    throw new RedirectsParseException(String.format("Target method value does not contain a value for \"%s\". %s", DST_NAME, targetMethodValue));
                }
                JsonElement newNameNode = targetMethodValue.get(DST_NAME);
                if (!newNameNode.isJsonPrimitive() || !newNameNode.getAsJsonPrimitive().isString()) {
                    throw new RedirectsParseException(String.format("Target method value does not contain a valid \"%s\". %s", DST_NAME, newNameNode));
                }

                String dstMethodName = throwOnLengthZero(newNameNode.getAsString(), () -> String.format("Target method has zero length value for key %s", methodRedirect));

                boolean makeSyntheticAccessor = getMakeSyntheticAccessorIfPresent(targetMethodValue);
                boolean shouldClone = getShouldCloneIfPresent(targetMethodValue);
                String mappingsOwner = getMappingsOwnerIfPresent(targetMethodValue);

                output.addTarget(new ClassTarget.TargetMethod(ownerName, returnType, mappingsOwner, srcMethodName, dstMethodName, shouldClone, makeSyntheticAccessor));
            } else {
                throw new RedirectsParseException(String.format("Could not parse Target method %s", methodRedirect));
            }
        }
    }

    private void parseTypeRedirects(RedirectSet output, Set<Map.Entry<String, JsonElement>> typeRedirects) throws RedirectsParseException {
        for (Map.Entry<String, JsonElement> typeRedirect : typeRedirects) {
            JsonElement value = typeRedirect.getValue();
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                throw new RedirectsParseException(String.format("Type redirect value has invalid structure: %s", value));
            }
            output.addRedirect(new RedirectSet.TypeRedirect(
                    throwOnLengthZero(typeRedirect.getKey(), () -> String.format("Type redirect has zero length string key: %s", typeRedirect)),
                    throwOnLengthZero(value.getAsString(), () -> String.format("Type redirect has zero length string value: %s", typeRedirect))
            ));
        }
    }

    private void parseFieldRedirects(RedirectSet output, Set<Map.Entry<String, JsonElement>> fieldRedirects) throws RedirectsParseException {
        for (Map.Entry<String, JsonElement> fieldRedirect : fieldRedirects) {
            ImmutableTriple<String, String, String> ownerIdentType = parseSignature(fieldRedirect.getKey());
            String ownerName = ownerIdentType.left;
            String srcFieldName = ownerIdentType.middle;
            String fieldType = ownerIdentType.right;

            JsonElement value = fieldRedirect.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                output.addRedirect(new RedirectSet.FieldRedirect(
                        ownerName,
                        fieldType,
                        null,
                        srcFieldName,
                        throwOnLengthZero(value.getAsString(), () -> String.format("Field redirect has zero length string value: %s", fieldRedirect))
                ));
            } else if (value.isJsonObject()) { // field redirect might contain a mappings owner
                JsonObject fieldRedirectValue = value.getAsJsonObject();

                if (!fieldRedirectValue.has(DST_NAME)) {
                    throw new RedirectsParseException(String.format("Field redirect value does not contain a value for \"%s\". %s", DST_NAME, fieldRedirectValue));
                }
                JsonElement newNameNode = fieldRedirectValue.get(DST_NAME);
                if (!newNameNode.isJsonPrimitive() || !newNameNode.getAsJsonPrimitive().isString()) {
                    throw new RedirectsParseException(String.format("Field redirect value does not contain a valid \"%s\". %s", DST_NAME, newNameNode));
                }

                String dstFieldName = throwOnLengthZero(newNameNode.getAsString(), () -> String.format("Field redirect has zero length value for key %s", fieldRedirect));
                String mappingsOwner = getMappingsOwnerIfPresent(fieldRedirectValue);

                output.addRedirect(new RedirectSet.FieldRedirect(ownerName, fieldType, mappingsOwner, srcFieldName, dstFieldName));
            } else {
                throw new RedirectsParseException(String.format("Field redirect value has invalid structure: %s", value));
            }
        }
    }

    private void parseMethodRedirects(RedirectSet output, Set<Map.Entry<String, JsonElement>> methodRedirects) throws RedirectsParseException {
        for (Map.Entry<String, JsonElement> methodRedirect : methodRedirects) {
            ImmutableTriple<String, String, String> ownerIdentType = parseSignature(methodRedirect.getKey());
            String ownerName = ownerIdentType.left;
            String srcMethodName = ownerIdentType.middle;
            String returnType = ownerIdentType.right;

            JsonElement value = methodRedirect.getValue();
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String dstMethodName = throwOnLengthZero(value.getAsString(), () -> String.format("Method redirect has zero length value for key %s", methodRedirect));
                output.addRedirect(new RedirectSet.MethodRedirect(ownerName, returnType, null, srcMethodName, dstMethodName));
            } else if (value.isJsonObject()) { // method redirect might contain a mappings owner
                JsonObject methodRedirectValue = value.getAsJsonObject();

                if (!methodRedirectValue.has(DST_NAME)) {
                    throw new RedirectsParseException(String.format("Method redirect value does not contain a value for \"%s\". %s", DST_NAME, methodRedirectValue));
                }
                JsonElement newNameNode = methodRedirectValue.get(DST_NAME);
                if (!newNameNode.isJsonPrimitive() || !newNameNode.getAsJsonPrimitive().isString()) {
                    throw new RedirectsParseException(String.format("Method redirect value does not contain a valid \"%s\". %s", DST_NAME, newNameNode));
                }

                String dstMethodName = throwOnLengthZero(newNameNode.getAsString(), () -> String.format("Method redirect has zero length value for key %s", methodRedirect));
                String mappingsOwner = getMappingsOwnerIfPresent(methodRedirectValue);

                output.addRedirect(new RedirectSet.MethodRedirect(ownerName, returnType, mappingsOwner, srcMethodName, dstMethodName));
            } else {
                throw new RedirectsParseException(String.format("Could not parse Method redirect %s", methodRedirect));
            }
        }
    }

    private boolean getMakeSyntheticAccessorIfPresent(JsonObject redirectElement) throws RedirectsParseException {
        boolean makeSyntheticAccessor = false;
        if (redirectElement.has(MAKE_SYNTHETIC_ACCESSOR_NAME)) { // synthetic accessor is optional
            JsonElement syntheticAccessorNode = redirectElement.get(MAKE_SYNTHETIC_ACCESSOR_NAME);
            if (!syntheticAccessorNode.isJsonPrimitive() || !syntheticAccessorNode.getAsJsonPrimitive().isBoolean()) {
                throw new RedirectsParseException(String.format("Redirect value does not contain a valid \"%s\". %s", MAKE_SYNTHETIC_ACCESSOR_NAME, syntheticAccessorNode));
            }
            makeSyntheticAccessor = syntheticAccessorNode.getAsBoolean();
        }
        return makeSyntheticAccessor;
    }

    private boolean getShouldCloneIfPresent(JsonObject redirectElement) throws RedirectsParseException {
        boolean shouldClone = true;
        if (redirectElement.has(SHOULD_CLONE_NAME)) { // synthetic accessor is optional
            JsonElement syntheticAccessorNode = redirectElement.get(SHOULD_CLONE_NAME);
            if (!syntheticAccessorNode.isJsonPrimitive() || !syntheticAccessorNode.getAsJsonPrimitive().isBoolean()) {
                throw new RedirectsParseException(String.format("Redirect value does not contain a valid \"%s\". %s", SHOULD_CLONE_NAME, syntheticAccessorNode));
            }
            shouldClone = syntheticAccessorNode.getAsBoolean();
        }
        return shouldClone;
    }

    private String getMappingsOwnerIfPresent(JsonObject targetMethodValue)
            throws RedirectsParseException {
        String mappingsOwner = null;
        if (targetMethodValue.has(MAPPINGS_OWNER_NAME)) { // mappings owner is optional
            JsonElement mappingsOwnerNode = targetMethodValue.get(MAPPINGS_OWNER_NAME);
            if (!mappingsOwnerNode.isJsonPrimitive() || !mappingsOwnerNode.getAsJsonPrimitive().isString()) {
                throw new RedirectsParseException(String.format("Redirect value does not contain a valid \"%s\". %s", MAPPINGS_OWNER_NAME, mappingsOwnerNode));
            }
            mappingsOwner = throwOnLengthZero(mappingsOwnerNode.getAsString(),
                    () -> String.format("Field redirect has zero length value for %s: %s", MAPPINGS_OWNER_NAME, mappingsOwnerNode));
        }
        return mappingsOwner;
    }

    /**
     * Accepts signature like owner#ident;type
     * <p>eg: {@code net.minecraft.world.level.chunk.LevelChunk#sections;net.minecraft.world.level.chunk.LevelChunkSection}</p>
     */
    public static ImmutableTriple<String, String, String> parseSignature(String in) throws RedirectsParseException {
        String s = throwOnLengthZero(in, () -> "Signature has zero length");

        int ownerToIdentSeparator = s.lastIndexOf("#");
        int identToTypeSeparator = s.lastIndexOf(";");

        if (ownerToIdentSeparator == -1 || identToTypeSeparator == -1 || ownerToIdentSeparator >= identToTypeSeparator) {
            // key contained no # symbol or no ; symbol, or they were in the wrong order
            throw new RedirectsParseException(String.format("Invalid \"owner#ident;type\" signature: %s", s));
        }

        String owner = throwOnLengthZero(s.substring(0, ownerToIdentSeparator),
                () -> String.format("Invalid \"owner#ident;type\" signature: %s", s));
        String ident = throwOnLengthZero(s.substring(ownerToIdentSeparator + 1, identToTypeSeparator),
                () -> String.format("Invalid \"owner#ident;type\" signature: %s", s));
        String type = throwOnLengthZero(s.substring(identToTypeSeparator + 1, s.length()),
                () -> String.format("Invalid \"owner#ident;type\" signature: %s", s));

        return new ImmutableTriple<>(owner, ident, type);
    }

    private static String throwOnLengthZero(String string, Supplier<String> message) throws RedirectsParseException {
        if (string.length() < 1) {
            throw new RedirectsParseException(message.get());
        }
        return string;
    }

    public static class ClassTarget {
        private final String className;
        private final List<String> usesSets = new ArrayList<>();
        private final List<TargetMethod> targetMethods = new ArrayList<>();
        private boolean wholeClass = false;

        public ClassTarget(String className) {
            this.className = className;
        }

        public void addUsesSet(String usesSet) {
            this.usesSets.add(usesSet);
        }

        public void addTarget(TargetMethod targetMethod) {
            if (wholeClass) {
                throw new IllegalStateException("Cannot add target methods when targeting whole class!");
            }
            this.targetMethods.add(targetMethod);
        }

        /**
         * Specifies targeting a whole class, without cloning methods. Allows in-place redirects for the whole class
         */
        public void targetWholeClass() {
            if (!targetMethods.isEmpty()) {
                throw new IllegalStateException("Cannot add target whole class when method targets are specified!");
            }
            wholeClass = true;
        }

        public String getClassName() {
            return className;
        }

        public List<String> getSets() {
            return Collections.unmodifiableList(usesSets);
        }

        public boolean isWholeClass() {
            return wholeClass;
        }

        public List<TargetMethod> getTargetMethods() {
            return Collections.unmodifiableList(targetMethods);
        }

        public static final class TargetMethod {
            private final String owner;
            private final String returnType;
            private final @Nullable String mappingsOwner;
            private final String srcMethodName;
            private final String dstMethodName;
            private final boolean shouldClone;
            private final boolean makeSyntheticAccessor;

            public TargetMethod(String owner, String returnType, @Nullable String mappingsOwner, String srcMethodName, String dstMethodName, boolean shouldClone, boolean makeSyntheticAccessor) {
                this.owner = owner;
                this.returnType = returnType;
                this.mappingsOwner = mappingsOwner;
                this.srcMethodName = srcMethodName;
                this.dstMethodName = dstMethodName;
                this.shouldClone = shouldClone;
                this.makeSyntheticAccessor = makeSyntheticAccessor;
            }

            public String owner() {
                return owner;
            }

            public String returnType() {
                return returnType;
            }

            public @Nullable String mappingsOwner() {
                return mappingsOwner;
            }

            public String srcMethodName() {
                return srcMethodName;
            }

            public String dstMethodName() {
                return dstMethodName;
            }

            public boolean shouldClone() {
                return shouldClone;
            }

            public boolean makeSyntheticAccessor() {
                return makeSyntheticAccessor;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                TargetMethod that = (TargetMethod) obj;
                return Objects.equals(this.owner, that.owner) &&
                        Objects.equals(this.returnType, that.returnType) &&
                        Objects.equals(this.mappingsOwner, that.mappingsOwner) &&
                        Objects.equals(this.srcMethodName, that.srcMethodName) &&
                        Objects.equals(this.dstMethodName, that.dstMethodName) &&
                        this.shouldClone == that.shouldClone &&
                        this.makeSyntheticAccessor == that.makeSyntheticAccessor;
            }

            @Override
            public int hashCode() {
                return Objects.hash(owner, returnType, mappingsOwner, srcMethodName, dstMethodName, shouldClone, makeSyntheticAccessor);
            }

            @Override
            public String toString() {
                return "TargetMethod[" +
                        "owner=" + owner + ", " +
                        "returnType=" + returnType + ", " +
                        "mappingsOwner=" + mappingsOwner + ", " +
                        "srcMethodName=" + srcMethodName + ", " +
                        "dstMethodName=" + dstMethodName + ", " +
                        "shouldClone=" + shouldClone + ", " +
                        "makeSyntheticAccessor=" + makeSyntheticAccessor + ']';
            }

                }
    }

    public static class RedirectSet {
        private final String name;
        private final List<TypeRedirect> typeRedirects = new ArrayList<>();
        private final List<FieldRedirect> fieldRedirects = new ArrayList<>();
        private final List<MethodRedirect> methodRedirects = new ArrayList<>();

        public RedirectSet(String name) {
            this.name = name;
        }

        public void addRedirect(TypeRedirect redirect) {
            this.typeRedirects.add(redirect);
        }

        public void addRedirect(FieldRedirect redirect) {
            this.fieldRedirects.add(redirect);
        }

        public void addRedirect(MethodRedirect redirect) {
            this.methodRedirects.add(redirect);
        }

        public String getName() {
            return name;
        }

        @NotNull public List<TypeRedirect> getTypeRedirects() {
            return Collections.unmodifiableList(this.typeRedirects);
        }

        @NotNull public List<FieldRedirect> getFieldRedirects() {
            return Collections.unmodifiableList(this.fieldRedirects);
        }

        @NotNull public List<MethodRedirect> getMethodRedirects() {
            return Collections.unmodifiableList(methodRedirects);
        }

        public static final class TypeRedirect {
            private final String srcClassName;
            private final String dstClassName;

            public TypeRedirect(String srcClassName, String dstClassName) {
                this.srcClassName = srcClassName;
                this.dstClassName = dstClassName;
            }

            public String srcClassName() {
                return srcClassName;
            }

            public String dstClassName() {
                return dstClassName;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                TypeRedirect that = (TypeRedirect) obj;
                return Objects.equals(this.srcClassName, that.srcClassName) &&
                        Objects.equals(this.dstClassName, that.dstClassName);
            }

            @Override
            public int hashCode() {
                return Objects.hash(srcClassName, dstClassName);
            }

            @Override
            public String toString() {
                return "TypeRedirect[" +
                        "srcClassName=" + srcClassName + ", " +
                        "dstClassName=" + dstClassName + ']';
            }
        }

        public static final class FieldRedirect {
            private final String owner;
            private final String fieldDesc;
            private final @Nullable String mappingsOwner;
            private final String srcFieldName;
            private final String dstFieldName;

            public FieldRedirect(String owner, String fieldDesc, @Nullable String mappingsOwner, String srcFieldName, String dstFieldName) {
                this.owner = owner;
                this.fieldDesc = fieldDesc;
                this.mappingsOwner = mappingsOwner;
                this.srcFieldName = srcFieldName;
                this.dstFieldName = dstFieldName;
            }

            public String owner() {
                return owner;
            }

            public String fieldDesc() {
                return fieldDesc;
            }

            public @Nullable String mappingsOwner() {
                return mappingsOwner;
            }

            public String srcFieldName() {
                return srcFieldName;
            }

            public String dstFieldName() {
                return dstFieldName;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                FieldRedirect that = (FieldRedirect) obj;
                return Objects.equals(this.owner, that.owner) &&
                        Objects.equals(this.fieldDesc, that.fieldDesc) &&
                        Objects.equals(this.mappingsOwner, that.mappingsOwner) &&
                        Objects.equals(this.srcFieldName, that.srcFieldName) &&
                        Objects.equals(this.dstFieldName, that.dstFieldName);
            }

            @Override
            public int hashCode() {
                return Objects.hash(owner, fieldDesc, mappingsOwner, srcFieldName, dstFieldName);
            }

            @Override
            public String toString() {
                return "FieldRedirect[" +
                        "owner=" + owner + ", " +
                        "fieldDesc=" + fieldDesc + ", " +
                        "mappingsOwner=" + mappingsOwner + ", " +
                        "srcFieldName=" + srcFieldName + ", " +
                        "dstFieldName=" + dstFieldName + ']';
            }
        }

        public static final class MethodRedirect {
            private final String owner;
            private final String returnType;
            private final @Nullable String mappingsOwner;
            private final String srcMethodName;
            private final String dstMethodName;

            public MethodRedirect(String owner, String returnType, @Nullable String mappingsOwner, String srcMethodName, String dstMethodName) {
                this.owner = owner;
                this.returnType = returnType;
                this.mappingsOwner = mappingsOwner;
                this.srcMethodName = srcMethodName;
                this.dstMethodName = dstMethodName;
            }

            public String owner() {
                return owner;
            }

            public String returnType() {
                return returnType;
            }

            public @Nullable String mappingsOwner() {
                return mappingsOwner;
            }

            public String srcMethodName() {
                return srcMethodName;
            }

            public String dstMethodName() {
                return dstMethodName;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) return true;
                if (obj == null || obj.getClass() != this.getClass()) return false;
                MethodRedirect that = (MethodRedirect) obj;
                return Objects.equals(this.owner, that.owner) &&
                        Objects.equals(this.returnType, that.returnType) &&
                        Objects.equals(this.mappingsOwner, that.mappingsOwner) &&
                        Objects.equals(this.srcMethodName, that.srcMethodName) &&
                        Objects.equals(this.dstMethodName, that.dstMethodName);
            }

            @Override
            public int hashCode() {
                return Objects.hash(owner, returnType, mappingsOwner, srcMethodName, dstMethodName);
            }

            @Override
            public String toString() {
                return "MethodRedirect[" +
                        "owner=" + owner + ", " +
                        "returnType=" + returnType + ", " +
                        "mappingsOwner=" + mappingsOwner + ", " +
                        "srcMethodName=" + srcMethodName + ", " +
                        "dstMethodName=" + dstMethodName + ']';
            }
        }
    }
}
