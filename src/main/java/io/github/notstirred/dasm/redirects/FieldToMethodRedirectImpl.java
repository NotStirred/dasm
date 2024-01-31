package io.github.notstirred.dasm.redirects;

import io.github.notstirred.dasm.data.ClassField;
import io.github.notstirred.dasm.data.ClassMethod;
import lombok.Data;

@Data
public class FieldToMethodRedirectImpl {
    public final ClassField field;
    public final ClassMethod method;
}
