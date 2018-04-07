package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.function.Function;

/**
 * Overrides JsonNode object values from a map of properties.
 *
 * @since 0.17
 */
public class InPlaceMapOverrider implements Function<JsonNode, JsonNode> {

    private Map<String, String> properties;

    public InPlaceMapOverrider(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public JsonNode apply(JsonNode t) {
        properties.entrySet().forEach(e -> {

            PathSegment target = lastPathComponent(t, e.getKey());
            target.fillMissingParents();

            if (target.getParent() == null) {
                throw new IllegalArgumentException("No parent node");
            }

            target.getParent().writeChild(target.getIncomingPath(), e.getValue());
        });

        return t;
    }

    protected PathSegment<?> lastPathComponent(JsonNode t, String path) {
        return PathSegment.create(t, path).lastPathComponent().get();
    }
}
