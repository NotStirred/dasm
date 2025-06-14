package io.github.notstirred.dasm.transformer.exception;

import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.exception.DasmException;

public class FieldRedirectingToFieldAndMethod extends DasmException {
    public FieldRedirectingToFieldAndMethod(FieldRedirectImpl fieldRedirect, FieldToMethodRedirectImpl fieldToMethodRedirect) {
        super("FieldRedirect for `" + fieldRedirect.srcField().name() + "` redirects to another field `" +
                fieldRedirect.dstOwner() + "#" + fieldRedirect.dstName() + "`, and a method `" +
                fieldToMethodRedirect.getterDstMethod().owner() + "#" + fieldToMethodRedirect.getterDstMethod().method().getName() + "`"
        );
    }
}
