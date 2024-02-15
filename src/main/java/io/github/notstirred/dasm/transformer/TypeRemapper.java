package io.github.notstirred.dasm.transformer;

import io.github.notstirred.dasm.api.provider.MappingsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Remapper;

import java.util.HashMap;
import java.util.Map;

public class TypeRemapper extends Remapper {
    public static final String SKIP_TYPE_REDIRECT_PREFIX = "dasm_redirect[";

    private static final Logger LOGGER = LogManager.getLogger();

    private final Map<String, String> typeRedirects;

    private final boolean debugLogging;

    public TypeRemapper(Map<Type, Type> typeRedirectsIn, boolean debugLogging, MappingsProvider mappingsProvider) {
        this.debugLogging = debugLogging;

        this.typeRedirects = new HashMap<>();
        for (Type type : typeRedirectsIn.keySet()) {
            typeRedirects.put(mappingsProvider.remapType(type).getInternalName(), mappingsProvider.remapType(typeRedirectsIn.get(type)).getInternalName());
        }

        typeRedirects.forEach((old, n) -> LOGGER.info("Type mapping: " + old + " -> " + n));

    }

    @Override
    public String map(final String key) {
        if (key.startsWith(SKIP_TYPE_REDIRECT_PREFIX)) {
            return key.substring(SKIP_TYPE_REDIRECT_PREFIX.length());
        }
        String mapped = typeRedirects.get(key);
        if (mapped == null) {
            if (this.debugLogging) {
                LOGGER.info("NOTE: handling CLASS redirect to self: " + key);
            }
            typeRedirects.put(key, key);
            return key;
        }
        return mapped;
    }
}
