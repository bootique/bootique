package io.bootique.annotation;

import com.google.inject.BindingAnnotation;
import io.bootique.command.Command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A binding annotation for a set of {@link Command}s, that have been
 * created by Bootique based on user configuration.
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface DecoratedCommands {

}
