package io.bootique.config.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

class YamlReader {
    static JsonNode read(String yaml) {

        ByteArrayInputStream in = new ByteArrayInputStream(yaml.getBytes());

        try {
            YAMLParser parser = new YAMLFactory().createParser(in);
            return new ObjectMapper().readTree(parser);
        } catch (IOException e) {
            throw new RuntimeException("Error reading yaml: " + yaml, e);
        }
    }
}
