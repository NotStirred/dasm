package io.github.notstirred.dasm.transformer.exception;

import io.github.notstirred.dasm.exception.DasmException;

public class FieldToMethodPutFieldWithoutSetterMethod extends DasmException {
    public FieldToMethodPutFieldWithoutSetterMethod(String owner, String name) {
        super("Found a PUTFIELD for field `" + owner + "#" + name + "` with a @FieldToMethodRedirect, but no setter method was defined");
    }
}
