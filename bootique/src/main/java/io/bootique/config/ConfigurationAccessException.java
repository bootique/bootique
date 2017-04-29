package io.bootique.config;

import io.bootique.BootiqueException;
import io.bootique.command.CommandOutcome;

import java.net.URL;
import java.util.Objects;

/**
 * An exception thrown when configuration resource is not accessible.
 *
 * @since 0.23
 */
public class ConfigurationAccessException extends BootiqueException {

    private URL resourceUrl;

    public ConfigurationAccessException(URL resourceUrl) {
        this(resourceUrl, null);
    }

    public ConfigurationAccessException(URL resourceUrl, Throwable cause) {
        super(CommandOutcome.failed(1, "Config resource '" + Objects.requireNonNull(resourceUrl) + "' is not found.", cause));
        this.resourceUrl = Objects.requireNonNull(resourceUrl);
    }

    public URL getResourceUrl() {
        return resourceUrl;
    }
}
