package io.github.notstirred.dasm.annotation;

import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import io.github.notstirred.dasm.api.annotations.Dasm;
import io.github.notstirred.dasm.exception.NoSuchTypeExists;
import io.github.notstirred.dasm.util.ClassNodeProvider;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import java.util.*;

public class AnnotationParser {
    private final ClassNodeProvider provider;
    private final Map<Type, RedirectSetImpl> redirectSetsByType = new HashMap<>();

    public AnnotationParser(ClassNodeProvider provider) {
        this.provider = provider;
    }

    public List<RedirectSetImpl> findRedirectSets(ClassNode targetClass) throws NoSuchTypeExists, RedirectSetImpl.RedirectSetParseException {
        AnnotationNode dasmAnnotation = AnnotationUtil.getAnnotationIfPresent(targetClass.invisibleAnnotations, Dasm.class);
        if (dasmAnnotation == null) {
            return Collections.emptyList(); // TODO: trace log that nothing found for this class?
        }

        List<RedirectSetImpl> foundRedirectSets = new ArrayList<>();

        Map<String, Object> values = AnnotationUtil.getAnnotationValues(dasmAnnotation, Dasm.class);
        @SuppressWarnings("unchecked") List<Type> sets = (List<Type>) values.get("value");
        for (Type redirectSetType : sets) {
            addRedirectSetsForType(redirectSetType, foundRedirectSets);
        }

        return foundRedirectSets;
    }

//    public void buildClassTarget(ClassNode targetClass, TargetClass classTarget, TransformFrom.ApplicationStage stage, String methodPrefix) {
//
//    }

    private void addRedirectSetsForType(Type redirectSetType,
                                        List<RedirectSetImpl> redirectSets) throws NoSuchTypeExists, RedirectSetImpl.RedirectSetParseException {
        RedirectSetImpl existingSet = this.redirectSetsByType.get(redirectSetType);
        if (existingSet != null) {
            redirectSets.add(existingSet);
            return;
        }

        ClassNode redirectSetClass = this.provider.classNode(redirectSetType);
        RedirectSetImpl redirectSet = RedirectSetImpl.parse(redirectSetClass, this.provider);
        this.redirectSetsByType.put(redirectSetType, redirectSet);
        redirectSets.add(redirectSet);

        for (Type superRedirectSet : redirectSet.superRedirectSets) {
            addRedirectSetsForType(superRedirectSet, redirectSets);
        }
    }
}
