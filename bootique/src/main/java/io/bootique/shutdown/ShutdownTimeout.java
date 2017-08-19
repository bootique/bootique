package io.bootique.shutdown;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

/**
 * Annotates {@link ShutdownManager} timeout injection point. Timeout value
 * should be a {@link Duration}.
 * 
 * @since 0.11
 * @deprecated  0.23 unused as shutdown management initialization is moved to {@link io.bootique.Bootique} class.
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
@Deprecated
public @interface ShutdownTimeout {

}
