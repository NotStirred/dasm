package io.github.notstirred.dasm.transformer.data;

import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.*;
import io.github.notstirred.dasm.api.provider.MappingsProvider;
import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import lombok.Getter;
import org.objectweb.asm.Type;

import java.util.*;

import static io.github.notstirred.dasm.util.TypeUtil.typeNameToDescriptor;
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
            Map<Type, RedirectSetImpl.Container> containers = new HashMap<>();
            redirectSet.containers().forEach(container -> containers.put(container.type(), container));

            // Add inherited redirects from super containers.
            for (RedirectSetImpl.Container evaluatingContainer : redirectSet.containers()) {
                // Currently for every node we iterate down from the root super node building the redirects as we go,
                // resulting in no re-use of identical data.
                //
                // This entire section could be made FAR more efficient by only evaluating leaf containers and relying
                // on inheritance for the super nodes.
                // I decided it was unnecessary as massive inheritance trees are uncommon and the performance loss is
                // currently trivial.
                List<RedirectSetImpl.Container> containerHierarchy = new ArrayList<>();
                do {
                    containerHierarchy.add(evaluatingContainer);
                    evaluatingContainer = evaluatingContainer.superContainer();
                } while (evaluatingContainer != null);
                Collections.reverse(containerHierarchy);

                List<TypeRedirectImpl> typeR = new ArrayList<>();
                List<FieldRedirectImpl> fieldR = new ArrayList<>();
                List<MethodRedirectImpl> methodR = new ArrayList<>();
                List<FieldToMethodRedirectImpl> fieldToMethodR = new ArrayList<>();
                List<ConstructorToFactoryRedirectImpl> constructorToFactoryR = new ArrayList<>();

                for (RedirectSetImpl.Container container : containerHierarchy) {
                    RedirectSetImpl.Container parent = container.superContainer() != null ? container.superContainer() : container;
                    OwnerChanger ownerChanger = new OwnerChanger(
                            parent.srcType().getClassName(), container.srcType().getClassName(),
                            parent.dstType().getClassName(), container.dstType().getClassName()
                    );

                    typeR.addAll(container.typeRedirects());
                    typeR.forEach(typeRedirect -> {
                        typeRedirect = ownerChanger.remap(typeRedirect);
                        typeRedirects.put(
                                getType(typeNameToDescriptor(mappingsProvider.mapClassName(typeRedirect.srcType().getClassName()))),
                                new TypeAndIsInterface(
                                        getType(typeNameToDescriptor(mappingsProvider.mapClassName(typeRedirect.dstType().getClassName()))),
                                        typeRedirect.isDstInterface()
                                )
                        );
                    });

                    fieldR.addAll(container.fieldRedirects());
                    for (FieldRedirectImpl fieldRedirect : fieldR) {
                        fieldRedirect = ownerChanger.remap(fieldRedirect);
                        fieldRedirects.put(fieldRedirect.srcField(), fieldRedirect);
                    }

                    methodR.addAll(container.methodRedirects());
                    for (MethodRedirectImpl methodRedirect : methodR) {
                        methodRedirect = ownerChanger.remap(methodRedirect);
                        methodRedirects.put(methodRedirect.srcMethod(), methodRedirect);
                    }

                    fieldToMethodR.addAll(container.fieldToMethodRedirects());
                    for (FieldToMethodRedirectImpl fieldToMethodRedirect : fieldToMethodR) {
                        fieldToMethodRedirect = ownerChanger.remap(fieldToMethodRedirect);
                        fieldToMethodRedirects.put(fieldToMethodRedirect.srcField(), fieldToMethodRedirect);
                    }

                    constructorToFactoryR.addAll(container.constructorToFactoryRedirects());
                    for (ConstructorToFactoryRedirectImpl constructorToFactoryRedirect : constructorToFactoryR) {
                        constructorToFactoryRedirect = ownerChanger.remap(constructorToFactoryRedirect);
                        constructorToFactoryRedirects.put(constructorToFactoryRedirect.srcConstructor(), constructorToFactoryRedirect);
                    }
                }
            }
        }
    }

    public void addLambdaRedirect(ClassMethod classMethodLambda, MethodRedirectImpl redirect) {
        methodRedirects.put(classMethodLambda, redirect);
    }

    private static class OwnerChanger implements MappingsProvider {
        private final Map<String, String> typeMapping = new HashMap<>();

        OwnerChanger(String oldSrc, String newSrc, String oldDst, String newDst) {
            this.typeMapping.put(oldSrc, newSrc);
            this.typeMapping.put(oldDst, newDst);
        }

        @Override
        public String mapFieldName(String owner, String fieldName, String descriptor) {
            return fieldName;
        }

        @Override
        public String mapMethodName(String owner, String methodName, String descriptor) {
            return methodName;
        }

        @Override
        public String mapClassName(String className) {
            return typeMapping.getOrDefault(className, className);
        }

        private ClassField remap(ClassField classField) {
            return new ClassField(this.remapType(classField.owner()), this.remapType(classField.mappingsOwner()), classField.type(), classField.name());
        }

        private ClassMethod remap(ClassMethod classMethod) {
            return new ClassMethod(this.remapType(classMethod.owner()), this.remapType(classMethod.mappingsOwner()), classMethod.method());
        }

        public FieldToMethodRedirectImpl remap(FieldToMethodRedirectImpl redirect) {
            return new FieldToMethodRedirectImpl(
                    this.remap(redirect.srcField()),
                    this.remap(redirect.getterDstMethod()),
                    redirect.setterDstMethod().map(this::remap),
                    redirect.isStatic(),
                    redirect.isDstOwnerInterface()
            );
        }

        public ConstructorToFactoryRedirectImpl remap(ConstructorToFactoryRedirectImpl redirect) {
            return new ConstructorToFactoryRedirectImpl(
                    this.remap(redirect.srcConstructor()),
                    this.remapType(redirect.dstOwner()),
                    redirect.dstName(),
                    redirect.isDstOwnerInterface()
            );
        }

        public FieldRedirectImpl remap(FieldRedirectImpl redirect) {
            return new FieldRedirectImpl(
                    this.remap(redirect.srcField()),
                    this.remapType(redirect.dstOwner()),
                    redirect.dstName()
            );
        }

        public MethodRedirectImpl remap(MethodRedirectImpl redirect) {
            return new MethodRedirectImpl(
                    this.remap(redirect.srcMethod()),
                    this.remapType(redirect.dstOwner()),
                    redirect.dstName(),
                    redirect.isStatic(),
                    redirect.isDstOwnerInterface()
            );
        }

        public TypeRedirectImpl remap(TypeRedirectImpl redirect) {
            return new TypeRedirectImpl(
                    this.remapType(redirect.srcType()),
                    this.remapType(redirect.dstType()),
                    redirect.isDstInterface()
            );
        }
    }
}
