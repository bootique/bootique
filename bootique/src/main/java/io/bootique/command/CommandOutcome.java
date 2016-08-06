package io.bootique.command;

public class CommandOutcome {

	private String message;
	private int exitCode;
	private Throwable exception;

	public static CommandOutcome succeeded() {
		CommandOutcome o = new CommandOutcome();
		return o;
	}

	public static CommandOutcome failed(int exitCode, Throwable cause) {
		CommandOutcome o = succeeded();
		o.exitCode = exitCode;
		o.exception = cause;
		return o;
	}

	public static CommandOutcome failed(int exitCode, String message) {
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
		return exitCode == 0;
	}

	public void exit() {
		System.exit(exitCode);
	}

}
