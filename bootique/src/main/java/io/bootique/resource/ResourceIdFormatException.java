package io.bootique.resource;

import io.bootique.BootiqueException;
import io.bootique.command.CommandOutcome;

import java.util.Objects;

/**
 * @since 0.23
 */
public class ResourceIdFormatException extends BootiqueException {

    private String resourceId;

    public ResourceIdFormatException(String resourceId) {
        this(resourceId, null);
    }

    public ResourceIdFormatException(String resourceId, Throwable cause) {
        super(CommandOutcome.failed(1, "Invalid config resource '" + Objects.requireNonNull(resourceId) + "'.", cause));
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }
}
