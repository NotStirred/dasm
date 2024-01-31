package io.github.notstirred.dasm.annotation;

import io.github.notstirred.dasm.api.provider.ClassProvider;
import io.github.notstirred.dasm.annotation.parse.RedirectSetImpl;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnnotationParser {
    private final ClassProvider classProvider;
    private final Map<Type, List<RedirectSetImpl>> redirectSetsByType = new ConcurrentHashMap<>();

    public AnnotationParser(ClassProvider classProvider) {
        this.classProvider = classProvider;
    }

//    public void findRedirectSets(String targetClassName, ClassNode targetClass, Set<RedirectSet> redirectSets) {
//
//    }
//
//    public void buildClassTarget(ClassNode targetClass, TargetClass classTarget, TransformFrom.ApplicationStage stage, String methodPrefix) {
//
//    }
}
