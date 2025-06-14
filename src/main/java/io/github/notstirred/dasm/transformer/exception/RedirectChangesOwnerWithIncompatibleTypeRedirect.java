package io.github.notstirred.dasm.transformer.exception;


import io.github.notstirred.dasm.annotation.parse.redirects.FieldRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.FieldToMethodRedirectImpl;
import io.github.notstirred.dasm.annotation.parse.redirects.MethodRedirectImpl;
import io.github.notstirred.dasm.exception.DasmException;

public class RedirectChangesOwnerWithIncompatibleTypeRedirect extends DasmException {
    public RedirectChangesOwnerWithIncompatibleTypeRedirect(MethodRedirectImpl methodRedirect, String typeRedirectSrc, String typeRedirectDst) {
        super("@MethodRedirect for `" + methodRedirect.srcMethod().owner().getClassName() + "#" + methodRedirect.srcMethod().method().getName() + "` -> `" +
                methodRedirect.dstOwner().getClassName() + "#" + methodRedirect.dstName() + "`" +
                " has an incompatible owner change with @TypeRedirect `" +
                typeRedirectSrc.replace('/', '.') + "` -> `" + typeRedirectDst.replace('/', '.') + "`");
    }

    public RedirectChangesOwnerWithIncompatibleTypeRedirect(FieldRedirectImpl fieldRedirect, String typeRedirectSrc, String typeRedirectDst) {
        super("@FieldRedirect for `" + fieldRedirect.srcField().owner().getClassName() + "#" + fieldRedirect.srcField().name() + "` -> `" +
                fieldRedirect.dstOwner().getClassName() + "#" + fieldRedirect.dstName() +
                "` has an incompatible owner change with @TypeRedirect `" +
                typeRedirectSrc.replace('/', '.') + "` -> `" + typeRedirectDst.replace('/', '.') + "`");
    }

    public RedirectChangesOwnerWithIncompatibleTypeRedirect(FieldToMethodRedirectImpl fieldToMethodRedirect, String typeRedirectSrc, String typeRedirectDst) {
        super("@MethodRedirect for `" + fieldToMethodRedirect.srcField().owner().getClassName() + "#" + fieldToMethodRedirect.srcField().name() + "` -> `" +
                fieldToMethodRedirect.getterDstMethod().owner().getClassName() + "#" + fieldToMethodRedirect.getterDstMethod().method().getName() +
                "` has an incompatible owner change with @TypeRedirect `" +
                typeRedirectSrc.replace('/', '.') + "` -> `" + typeRedirectDst.replace('/', '.') + "`");
    }

}
