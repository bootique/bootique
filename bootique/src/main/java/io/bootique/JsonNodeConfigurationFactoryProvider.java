package io.bootique;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.ConfigurationSource;
import io.bootique.config.jackson.InPlaceResourceOverrider;
import io.bootique.config.jackson.InPlaceLeftHandMerger;
import io.bootique.config.jackson.InPlaceMapOverrider;
import io.bootique.config.jackson.JsonNodeConfigurationBuilder;
import io.bootique.config.jackson.JsonNodeConfigurationFactory;
import io.bootique.config.jackson.JsonNodeJsonParser;
import io.bootique.config.jackson.JsonNodeYamlParser;
import io.bootique.config.jackson.MultiFormatJsonNodeParser;
import io.bootique.config.jackson.MultiFormatJsonNodeParser.ParserType;
import io.bootique.env.Environment;
import io.bootique.jackson.JacksonService;
import io.bootique.log.BootLogger;
import io.bootique.meta.application.OptionMetadata;
import joptsimple.OptionSpec;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;

/**
 * @since 0.17
 */
public class JsonNodeConfigurationFactoryProvider implements Provider<ConfigurationFactory> {

    private ConfigurationSource configurationSource;
    private Environment environment;
    private JacksonService jacksonService;
    private BootLogger bootLogger;
    private Set<OptionMetadata> optionMetadataSet;
    private Cli cli;


    @Inject
    public JsonNodeConfigurationFactoryProvider(
            ConfigurationSource configurationSource,
            Environment environment,
            JacksonService jacksonService,
            BootLogger bootLogger,
            Set<OptionMetadata> optionMetadataSet,
            Cli cli) {

        this.configurationSource = configurationSource;
        this.environment = environment;
        this.jacksonService = jacksonService;
        this.bootLogger = bootLogger;
        this.optionMetadataSet = optionMetadataSet;
        this.cli = cli;
    }

    protected JsonNode loadConfiguration(Map<String, String> properties, Map<String, String> vars) {

        // hopefully sharing the mapper between parsers is safe... Does it
        // change the state during parse?
        ObjectMapper textToJsonMapper = jacksonService.newObjectMapper();
        Map<ParserType, Function<InputStream, Optional<JsonNode>>> parsers = new EnumMap<>(ParserType.class);
        parsers.put(ParserType.YAML, new JsonNodeYamlParser(textToJsonMapper));
        parsers.put(ParserType.JSON, new JsonNodeJsonParser(textToJsonMapper));

        Function<URL, Optional<JsonNode>> parser = new MultiFormatJsonNodeParser(parsers, bootLogger);

        BinaryOperator<JsonNode> singleConfigMerger = new InPlaceLeftHandMerger(bootLogger);
        Function<JsonNode, JsonNode> overrider = new InPlaceMapOverrider(properties, true, '.');

        if (!vars.isEmpty()) {
            overrider = overrider.andThen(new InPlaceMapOverrider(vars, false, '_'));
        }

        overrider = andCliOptionOverrider(overrider, parser, singleConfigMerger);

        return JsonNodeConfigurationBuilder.builder()
                .parser(parser)
                .merger(singleConfigMerger)
                .resources(configurationSource)
                .overrider(overrider)
                .build();
    }

    private Function<JsonNode, JsonNode> andCliOptionOverrider(
            Function<JsonNode, JsonNode> overrider,
            Function<URL, Optional<JsonNode>> parser,
            BinaryOperator<JsonNode> singleConfigMerger) {

        if (optionMetadataSet.isEmpty()) {
            return overrider;
        }

        List<OptionSpec<?>> detectedOptions = cli.detectedOptions();
        if (detectedOptions.isEmpty()) {
            return overrider;
        }

        // options tied to config property paths
        HashMap<String, String> options = new HashMap<>(5);

        // options tied to config resources
        List<URL> sources = new ArrayList<>(5);

        for (OptionSpec<?> cliOpt : detectedOptions) {

            List<String> optionNames = cliOpt.options();

            // TODO: allow lookup of option metadata by name to avoid linear scans...
            // Though we are dealing with small collection, so shouldn't be too horrible.

            for (OptionMetadata omd : optionMetadataSet) {

                if (!optionNames.contains(omd.getName())) {
                    continue;
                }

                if (omd.getConfigPath() != null) {
                    String cliValue = cli.optionString(omd.getName());
                    if (cliValue == null) {
                        cliValue = omd.getDefaultValue();
                    }

                    options.put(omd.getConfigPath(), cliValue);
                }

                if (omd.getConfigResource() != null) {
                    sources.add(omd.getConfigResource().getUrl());
                }
            }
        }

        if (!options.isEmpty()) {
            overrider = overrider.andThen(new InPlaceMapOverrider(options, true, '.'));
        }

        if (!sources.isEmpty()) {
            overrider = overrider.andThen(new InPlaceResourceOverrider(sources, parser, singleConfigMerger));
        }

        return overrider;
    }

    @Override
    public ConfigurationFactory get() {

        Map<String, String> vars = environment.frameworkVariables();
        Map<String, String> properties = environment.frameworkProperties();

        JsonNode rootNode = loadConfiguration(properties, vars);

        ObjectMapper jsonToObjectMapper = jacksonService.newObjectMapper();
        if (!vars.isEmpty()) {

            // switching to slower CI strategy for mapping properties...
            jsonToObjectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        }

        return new JsonNodeConfigurationFactory(rootNode, jsonToObjectMapper);
    }
}
