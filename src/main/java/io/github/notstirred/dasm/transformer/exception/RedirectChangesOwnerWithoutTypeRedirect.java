package io.github.notstirred.dasm.transformer.exception;

import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.exception.DasmException;

public class RedirectChangesOwnerWithoutTypeRedirect extends DasmException {
    public RedirectChangesOwnerWithoutTypeRedirect(MethodRedirectImpl methodRedirect) {
        super("@MethodRedirect for `" + methodRedirect.srcMethod().owner().getClassName() + "#" + methodRedirect.srcMethod().method().getName() + "` -> `" +
                methodRedirect.dstOwner().getClassName() + "#" + methodRedirect.dstName() +
                "` changes owner without a corresponding type redirect.");
    }

    public RedirectChangesOwnerWithoutTypeRedirect(FieldRedirectImpl fieldRedirect) {
        super("@FieldRedirect for `" + fieldRedirect.srcField().owner().getClassName() + "#" + fieldRedirect.srcField().name() + "` -> `" +
                fieldRedirect.dstOwner().getClassName() + "#" + fieldRedirect.dstName() +
                "` changes owner without a corresponding type redirect.");
    }

    public RedirectChangesOwnerWithoutTypeRedirect(FieldToMethodRedirectImpl fieldToMethodRedirect) {
        super("@FieldToMethodRedirect for `" + fieldToMethodRedirect.srcField().owner().getClassName() + "#" + fieldToMethodRedirect.srcField().name() +
                "` -> `" +
                fieldToMethodRedirect.getterDstMethod().owner().getClassName() + "#" + fieldToMethodRedirect.getterDstMethod().method().getName() +
                "` changes owner without a corresponding type redirect.");
    }
}
