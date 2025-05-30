package io.github.notstirred.dasm.api.annotations;

import io.github.notstirred.dasm.api.annotations.redirect.sets.RedirectSet;
import io.github.notstirred.dasm.api.annotations.selector.Ref;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Dasm {
    /**
     * The default {@link RedirectSet} to use when applying transforms within this Dasm class
     */
    Class<?>[] value();

    /**
     * By default, transforms are applied to the class with the {@link Dasm} annotation (a {@link SELF_TARGET SELF_TARGET})
     * <p>
     * Instead they may be applied to a different class by specifying it as the {@link #target()}. Dasm then acts <i>as if</i> the
     * transforms were found on the {@link #target()}
     */
    Ref target() default @Ref(SELF_TARGET.class);

    final class SELF_TARGET {
        private SELF_TARGET() {
        }
    }
}
