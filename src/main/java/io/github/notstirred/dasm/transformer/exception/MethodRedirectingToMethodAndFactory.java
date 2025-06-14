package io.github.notstirred.dasm.transformer.exception;

import io.github.notstirred.dasm.annotation.parse.redirects.ConstructorToFactoryRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.exception.DasmException;

public class MethodRedirectingToMethodAndFactory extends DasmException {
    public MethodRedirectingToMethodAndFactory(MethodRedirectImpl methodRedirect, ConstructorToFactoryRedirectImpl factoryRedirect) {
        super("MethodRedirect for `" + methodRedirect.srcMethod().method().getName() + "` redirects to another method `" +
                methodRedirect.dstOwner() + "#" + methodRedirect.dstName() + "`, and a factory `" +
                factoryRedirect.dstOwner() + "#" + factoryRedirect.dstName() + "`"
        );
    }
}
