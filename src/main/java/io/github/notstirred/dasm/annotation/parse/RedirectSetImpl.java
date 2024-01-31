package io.github.notstirred.dasm.annotation.parse;

import io.github.notstirred.dasm.redirects.*;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class RedirectSetImpl {
    private final Set<FieldToMethodRedirectImpl> fieldToMethodRedirects;
    private final Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects;
    private final Set<FieldRedirectImpl> fieldRedirects;
    private final Set<MethodRedirectImpl> methodRedirects;
    private final Set<TypeRedirectImpl> typeRedirects;

    public static Optional<RedirectSetImpl> parse(ClassNode redirectSetClass) {
        Set<FieldToMethodRedirectImpl> fieldToMethodRedirects = new HashSet<>();
        Set<ConstructorToFactoryRedirectImpl> constructorToFactoryRedirects = new HashSet<>();
        Set<FieldRedirectImpl> fieldRedirects = new HashSet<>();
        Set<MethodRedirectImpl> methodRedirects = new HashSet<>();
        Set<TypeRedirectImpl> typeRedirects = new HashSet<>();

        return Optional.empty();
    }
}
