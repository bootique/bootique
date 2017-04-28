package io.bootique.command;

public class CommandOutcome {

    // UNIX success exits code
    private static final int SUCCESS_EXIT_CODE = 0;

    private static final CommandOutcome SUCCESS = new CommandOutcome(SUCCESS_EXIT_CODE, null, null);

    private final String message;
    private final int exitCode;
    private final Throwable exception;

    public static CommandOutcome succeeded() {
        return SUCCESS;
    }

    public static CommandOutcome failed(int exitCode, Throwable cause) {
        if (exitCode == SUCCESS_EXIT_CODE) {
            throw new IllegalArgumentException("Success code '0' used for failure outcome.");
        }

        return new CommandOutcome(exitCode, null, cause);
    }

    public static CommandOutcome failed(int exitCode, String message) {
        if (exitCode == SUCCESS_EXIT_CODE) {
            throw new IllegalArgumentException("Success code '0' used for failure outcome.");
        }

        return new CommandOutcome(exitCode, message, null);
    }

    private CommandOutcome(int exitCode, String message, Throwable exception) {
        this.message = message;
        this.exitCode = exitCode;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public int getExitCode() {
        return exitCode;
    }

    public Throwable getException() {
        return exception;
    }

    public boolean isSuccess() {
        return exitCode == SUCCESS_EXIT_CODE;
    }

    public void exit() {
        System.exit(exitCode);
    }

}
