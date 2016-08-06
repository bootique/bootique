package io.bootique.log;

import java.util.function.Supplier;

/**
 * A special logger that can be used by services participating in the boot
 * sequence before the logging subsystem is started. It would usually log to
 * STDOUT/STDERR.
 */
public interface BootLogger {

	/**
	 * Outputs the message to STDERR only if BootLogger is in a trace mode. Uses
	 * message supplier, so that the actual message production can be
	 * conditionally skipped if tracing is not enabled.
	 * 
	 * @param messageSupplier
	 *            a supplier of a String message.
	 */
	void trace(Supplier<String> messageSupplier);

	void stdout(String message);

	void stderr(String message);

	void stderr(String message, Throwable th);
}
