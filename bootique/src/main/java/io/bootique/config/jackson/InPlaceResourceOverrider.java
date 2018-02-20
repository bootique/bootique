package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import java.net.URL;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * Overrides JsonNode object values from one configuration resource.
 *
 * @since 0.24
 */
public class InPlaceResourceOverrider implements Function<JsonNode, JsonNode> {

    private URL source;
    private Function<URL, Optional<JsonNode>> parser;
    private BinaryOperator<JsonNode> merger;

    public InPlaceResourceOverrider(URL source, Function<URL, Optional<JsonNode>> parser, BinaryOperator<JsonNode> merger) {
        this.source = source;
        this.parser = parser;
        this.merger = merger;
    }

    @Override
    public JsonNode apply(JsonNode jsonNode) {
        return parser.apply(source)
                .map(configNode -> merger.apply(jsonNode, configNode))
                .orElse(jsonNode);
    }
}
