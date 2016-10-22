package io.bootique.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for documenting configuration type.
 *
 * @since 0.21
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BQConfig {

    /**
     * Human-readable config type description.
     *
     * @return a String representing a human-readable config type description.
     */
    String value() default "";
}
