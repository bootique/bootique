package io.bootique.shutdown;

import java.util.Map;

/**
 * A service that can shutdown other services when the app is exiting.
 *
 * @since 0.11
 */
public interface ShutdownManager {

    /**
     * Registers an object whose "close" method needs to be invoked during shutdown.
     *
     * @param shutdownListener an object that needs to be notified on shutdown.
     */
    void addShutdownHook(AutoCloseable shutdownListener);

    /**
     * Executes shutdown, calling "close" method of all registered listeners.
     * @return a map of shutdown listeners to shutdown exceptions they generated, if any.
     */
    Map<?, ? extends Throwable> shutdown();
}
