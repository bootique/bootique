package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class InPlaceFileOverrider implements Function<JsonNode, JsonNode> {

    private LinkedList<URL> sources;
    private Function<URL, Optional<JsonNode>> parser;
    private BinaryOperator<JsonNode> merger;

    public InPlaceFileOverrider(List<URL> sources, Function<URL, Optional<JsonNode>> parser, BinaryOperator<JsonNode> merger) {
        this.sources = new LinkedList<>(sources);
        this.parser = parser;
        this.merger = merger;
    }

    @Override
    public JsonNode apply(JsonNode jsonNode) {
        JsonNode configNode = sources.stream()
                .map(parser::apply)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .reduce(merger)
                .orElseGet(() -> new ObjectNode(new JsonNodeFactory(true)));

        return merger.apply(jsonNode, configNode);
    }
}
