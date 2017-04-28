package io.bootique.command;

public class CommandOutcome {

    private static final CommandOutcome SUCCESS = new CommandOutcome();

    // UNIX success exits code
    private static final int SUCCESS_EXIT_CODE = 0;

    private String message;
    private int exitCode;
    private Throwable exception;

    public static CommandOutcome succeeded() {
        return SUCCESS;
    }

    public static CommandOutcome failed(int exitCode, Throwable cause) {
        if (exitCode == SUCCESS_EXIT_CODE) {
            throw new IllegalArgumentException("Success code '0' used for failure outcome.");
        }

        CommandOutcome o = succeeded();
        o.exitCode = exitCode;
        o.exception = cause;
        return o;
    }

    public static CommandOutcome failed(int exitCode, String message) {
        if (exitCode == SUCCESS_EXIT_CODE) {
            throw new IllegalArgumentException("Success code '0' used for failure outcome.");
        }

        CommandOutcome o = succeeded();
        o.exitCode = exitCode;
        o.message = message;
        return o;
    }

    private CommandOutcome() {
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
