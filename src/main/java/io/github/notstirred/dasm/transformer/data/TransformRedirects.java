package io.github.notstirred.dasm.transformer.data;

import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.*;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import lombok.Getter;
import org.objectweb.asm.Type;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static io.github.notstirred.dasm.util.TypeUtil.classNameToDescriptor;
import static org.objectweb.asm.Type.getType;


@Getter
public class TransformRedirects {
    private final Map<Type, TypeAndIsInterface> typeRedirects = new HashMap<>();
    private final Map<ClassField, FieldRedirectImpl> fieldRedirects = new HashMap<>();
    private final Map<ClassMethod, MethodRedirectImpl> methodRedirects = new HashMap<>();
    private final Map<ClassField, FieldToMethodRedirectImpl> fieldToMethodRedirects = new HashMap<>();
    private final Map<ClassMethod, ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects = new HashMap<>();

    public TransformRedirects(Collection<RedirectSetImpl> redirectSets, MappingsProvider mappingsProvider) {
        for (RedirectSetImpl redirectSet : redirectSets) {
            for (TypeRedirectImpl typeRedirect : redirectSet.typeRedirects()) {
                typeRedirects.put(
                        getType(classNameToDescriptor(mappingsProvider.mapClassName(typeRedirect.srcType().getClassName()))),
                        new TypeAndIsInterface(
                                getType(classNameToDescriptor(mappingsProvider.mapClassName(typeRedirect.dstType().getClassName()))),
                                typeRedirect.isDstInterface()
                        )
                );
            }

            for (FieldRedirectImpl fieldRedirect : redirectSet.fieldRedirects()) {
                fieldRedirects.put(fieldRedirect.srcField(), fieldRedirect);
            }

            for (MethodRedirectImpl methodRedirect : redirectSet.methodRedirects()) {
                methodRedirects.put(methodRedirect.srcMethod(), methodRedirect);
            }

            for (FieldToMethodRedirectImpl fieldToMethodRedirect : redirectSet.fieldToMethodRedirects()) {
                fieldToMethodRedirects.put(fieldToMethodRedirect.srcField(), fieldToMethodRedirect);
            }

            for (ConstructorToFactoryRedirectImpl constructorToFactoryRedirect : redirectSet.constructorToFactoryRedirects()) {
                constructorToFactoryRedirects.put(constructorToFactoryRedirect.srcConstructor(), constructorToFactoryRedirect);
            }
        }
    }

    public void addLambdaRedirect(ClassMethod classMethodLambda, MethodRedirectImpl redirect) {
        methodRedirects.put(classMethodLambda, redirect);
    }
}
