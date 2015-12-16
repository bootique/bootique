package com.nhl.bootique.log;

import java.util.function.Supplier;

public class DefaultBootLogger implements BootLogger {

	private boolean trace;

	public DefaultBootLogger(boolean trace) {
		this.trace = trace;
	}

	@Override
	public void trace(Supplier<String> messageSupplier) {
		if (trace) {
			stderr(messageSupplier.get());
		}
	}

	@Override
	public void stdout(String message) {
		System.out.println(message);
	}

	@Override
	public void stderr(String message) {
		System.err.println(message);
	}

	@Override
	public void stderr(String message, Throwable th) {
		stderr(message);
		th.printStackTrace(System.err);
	}
}
