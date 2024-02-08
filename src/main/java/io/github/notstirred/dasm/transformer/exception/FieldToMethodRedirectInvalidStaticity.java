package io.github.notstirred.dasm.transformer.exception;

import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.exception.DasmTransformException;
import lombok.Getter;

@Getter
public class FieldToMethodRedirectInvalidStaticity extends DasmTransformException {
    public FieldToMethodRedirectInvalidStaticity(FieldToMethodRedirectImpl fieldToMethodRedirect) {
        super("@FieldToMethodRedirect for `" + fieldToMethodRedirect.srcField().owner() + "#" + fieldToMethodRedirect.srcField().name() +
                "` is: " + (fieldToMethodRedirect.isStatic() ? "`static`" : "`non-static`") +
                ", expected: " + (!fieldToMethodRedirect.isStatic() ? "`static`" : "`non-static`"));
    }
}
