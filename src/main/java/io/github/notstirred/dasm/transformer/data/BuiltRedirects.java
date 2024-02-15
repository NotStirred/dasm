package io.github.notstirred.dasm.transformer.data;

import io.github.notstirred.dasm.annotation.parse.redirects.ConstructorToFactoryRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BuiltRedirects {
    /** Two type redirects maps exist for fast lookup for internal names, and descriptors */
    private final Map<String, String> typeRedirects;
    private final Map<String, String> typeRedirectsDescriptors;
    private final Map<String, FieldRedirectImpl> fieldRedirects;
    private final Map<String, MethodRedirectImpl> methodRedirects;
    private final Map<String, FieldToMethodRedirectImpl> fieldToMethodRedirects;
    private final Map<String, ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects;

    public BuiltRedirects(TransformRedirects redirects, MappingsProvider mappingsProvider) {
        this.typeRedirects = new HashMap<>();
        this.typeRedirectsDescriptors = new HashMap<>();
        redirects.typeRedirects().forEach((srcType, dstType) -> {
            typeRedirects.put(srcType.getInternalName(), dstType.type().getInternalName());
            typeRedirectsDescriptors.put(srcType.getDescriptor(), dstType.type().getDescriptor());
        });

        this.methodRedirects = new HashMap<>();
        redirects.methodRedirects().forEach((classMethodUnmapped, methodRedirect) -> {
            ClassMethod classMethod = classMethodUnmapped.remap(mappingsProvider);
            methodRedirects.put(
                    classMethod.owner().getInternalName() + "." + classMethod.method().getName() + classMethod.method().getDescriptor(),
                    methodRedirect
            );
        });

        this.fieldRedirects = new HashMap<>();
        redirects.fieldRedirects().forEach((classFieldUnmapped, fieldRedirect) -> {
            ClassField classField = classFieldUnmapped.remap(mappingsProvider);
            fieldRedirects.put(
                    classField.owner().getInternalName() + "." + classField.name(),
                    fieldRedirect
            );
        });

        this.fieldToMethodRedirects = new HashMap<>();
        redirects.fieldToMethodRedirects().forEach((classFieldUnmapped, fieldToMethodRedirect) -> {
            ClassField classField = classFieldUnmapped.remap(mappingsProvider);
            fieldToMethodRedirects.put(
                    classField.owner().getInternalName() + "." + classField.name(),
                    fieldToMethodRedirect
            );
        });

        this.constructorToFactoryRedirects = new HashMap<>();
        redirects.constructorToFactoryRedirects().forEach((classMethodUnmapped, constructorToFactoryRedirect) -> {
            ClassMethod classMethod = classMethodUnmapped.remap(mappingsProvider);
            constructorToFactoryRedirects.put(
                    classMethod.owner().getInternalName() + "." + classMethod.method().getName() + classMethod.method().getDescriptor(),
                    constructorToFactoryRedirect
            );
        });
    }
}
