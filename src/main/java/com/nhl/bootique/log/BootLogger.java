package com.nhl.bootique.log;

/**
 * A special logger that can be used by services participating in the boot
 * sequence before the logging subsystem is started. It would usually log to
 * STDOUT/STDERR.
 */
public interface BootLogger {

	void stdout(String message);
	
	void stderr(String message);
}
