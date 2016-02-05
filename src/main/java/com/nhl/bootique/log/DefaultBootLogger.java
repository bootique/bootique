package com.nhl.bootique.log;

import java.io.PrintStream;
import java.util.function.Supplier;

public class DefaultBootLogger implements BootLogger {

	private boolean trace;
	private PrintStream stdout;
	private PrintStream stderr;

	public DefaultBootLogger(boolean trace) {
		this(trace, System.out, System.err);
	}

	/**
	 * @since 0.12
	 */
	public DefaultBootLogger(boolean trace, PrintStream stdout, PrintStream stderr) {
		this.trace = trace;
		this.stderr = stderr;
		this.stdout = stdout;
	}

	@Override
	public void trace(Supplier<String> messageSupplier) {
		if (trace) {
			stderr(messageSupplier.get());
		}
	}

	@Override
	public void stdout(String message) {
		stdout.println(message);
	}

	@Override
	public void stderr(String message) {
		stderr.println(message);
	}

	@Override
	public void stderr(String message, Throwable th) {
		stderr(message);
		th.printStackTrace(System.err);
	}
}
