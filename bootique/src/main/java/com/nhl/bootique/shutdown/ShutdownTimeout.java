package com.nhl.bootique.shutdown;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;

import com.google.inject.BindingAnnotation;

/**
 * Annotates {@link ShutdownManager} timeout injection point. Timeout value
 * should be a {@link Duration}.
 * 
 * @since 0.11
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface ShutdownTimeout {

}
