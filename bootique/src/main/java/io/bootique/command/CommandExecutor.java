package io.bootique.command;

import com.google.inject.BindingAnnotation;
import io.bootique.CommandDecorator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A binding annotation for the executor service, that should be used for running simultaneous commands.
 *
 * @see io.bootique.BQCoreModuleExtender#decorateCommand(Class, CommandDecorator)
 * @since 0.25
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@BindingAnnotation
public @interface CommandExecutor {
}
