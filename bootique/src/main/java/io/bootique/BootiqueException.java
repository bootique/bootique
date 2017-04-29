package io.bootique;

import io.bootique.command.CommandOutcome;

import java.util.Objects;

/**
 * An exception originating in Bootique that indicates an app-wise configuration error, such as invalid CLI parameters,
 * bad YAML format, etc. Usually only its "outcome" property is of any interest (i.e. <i>what</i> happened). Its stack
 * trace (i.e. the place <i>where</i> it happened) is rarely important.
 *
 * @since 2.3
 */
public class BootiqueException extends RuntimeException {

    private CommandOutcome outcome;

    public BootiqueException(CommandOutcome outcome) {
        this.outcome = Objects.requireNonNull(outcome);
    }

    public CommandOutcome getOutcome() {
        return outcome;
    }

    @Override
    public Throwable getCause() {
        return outcome.getException();
    }

    @Override
    public String getMessage() {
        return outcome.getMessage();
    }
}
