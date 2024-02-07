package io.github.notstirred.dasm.annotation.parse.redirects;

import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import lombok.Data;

import java.util.Optional;

@Data
public class FieldToMethodRedirectImpl {
    private final ClassField srcField;
    private final ClassMethod getterDstMethod;
    private final Optional<ClassMethod> setterDstMethod;
    private final boolean isStatic;
    private final boolean isDstOwnerInterface;
}
