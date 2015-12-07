package com.nhl.launcher;

/**
 * An exception that might be thrown by a command to request exiting the process
 * with a specified non-zero code.
 */
public class SystemExitException extends RuntimeException {

	private static final long serialVersionUID = 2258038580196236091L;

	private int exitCode;

	public SystemExitException(int exitCode) {
		this.exitCode = exitCode;
	}

	public SystemExitException(int exitCode, String message, Throwable cause) {
		super(message, cause);
		this.exitCode = exitCode;
	}

	public SystemExitException(int exitCode, String message) {
		super(message);
		this.exitCode = exitCode;
	}

	public SystemExitException(int exitCode, Throwable cause) {
		super(cause);
		this.exitCode = exitCode;
	}

	public int getExitCode() {
		return exitCode;
	}
}
