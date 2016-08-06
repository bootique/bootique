package io.bootique.shutdown;

import java.util.Map;

/**
 * A service that can shutdown other services when the app is exiting.
 * 
 * @since 0.11
 */
public interface ShutdownManager {

	void addShutdownHook(AutoCloseable shutdownHook);

	Map<?, ? extends Throwable> shutdown();
}
