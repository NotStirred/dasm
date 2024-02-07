package io.github.notstirred.dasm.transformer.exception;


import io.github.notstirred.dasm.exception.DasmTransformException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RedirectChangesOwnerWithIncompatibleTypeRedirect extends DasmTransformException {
}
