package io.bootique;

import io.bootique.command.CommandOutcome;

/**
 * An exception originating in Bootique that indicates an app-wise configuration error, such as invalid CLI parameters,
 * bad YAML format, etc. Usually only its "outcome" property is of any interest (i.e. <i>what</i> happened). Its stack
 * trace (i.e. the place <i>where</i> it happened) is rarely important.
 *
 * @since 2.3
 */
public class BootiqueException extends RuntimeException {

    private CommandOutcome outcome;

    public BootiqueException(int exitCode, String message) {
        this.outcome = CommandOutcome.failed(exitCode, message, this);
    }

    public BootiqueException(int exitCode, String message, Throwable cause) {
        this.outcome = CommandOutcome.failed(exitCode, message, cause);
    }

    public CommandOutcome getOutcome() {
        return outcome;
    }

    @Override
    public Throwable getCause() {
        return outcome.getException() != this ? outcome.getException() : null;
    }

    @Override
    public String getMessage() {
        return outcome.getMessage();
    }
}
