package io.bootique.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for documenting configuration property of a type annotated with {@link BQConfig}. Should be applied to
 * setters.
 *
 * @since 0.21
 */
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface BQConfigProperty {

    /**
     * Human-readable property description.
     *
     * @return a String representing a human-readable property description.
     */
    String value() default "";
}
