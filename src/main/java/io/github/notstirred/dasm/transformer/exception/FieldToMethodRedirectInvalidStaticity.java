package io.github.notstirred.dasm.transformer.exception;

import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.exception.DasmTransformException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FieldToMethodRedirectInvalidStaticity extends DasmTransformException {
    private final FieldToMethodRedirectImpl fieldToMethodRedirect;
}
