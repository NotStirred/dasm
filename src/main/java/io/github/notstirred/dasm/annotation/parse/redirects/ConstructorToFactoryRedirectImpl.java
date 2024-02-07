package io.github.notstirred.dasm.annotation.parse.redirects;

import io.github.notstirred.dasm.data.ClassMethod;
import lombok.Data;

@Data
public class ConstructorToFactoryRedirectImpl {
    private final ClassMethod srcMethod;
}
