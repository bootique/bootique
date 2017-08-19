package io.bootique;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.ConfigurationSource;
import io.bootique.config.jackson.InPlaceFileOverrider;
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
import java.util.stream.Collectors;

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

        if (optionMetadataSet != null && !optionMetadataSet.isEmpty()) {

            List<OptionSpec<?>> detectedOptions = cli.detectedOptions();
            if (!detectedOptions.isEmpty()) {

                // options tied to config paths
                HashMap<String, String> options = new HashMap<>();

                // options tied to a config resources
                List<URL> sources = new ArrayList<>();

                for (OptionSpec<?> cliOpt : detectedOptions) {

                    List<String> detectedOptionStrings = cliOpt.options();

                    options.putAll(optionMetadataSet.stream()
                            .filter(o -> o.getConfigPath() != null && cliOpt.options().contains(o.getName()))
                            .collect(Collectors.toMap(o -> o.getConfigPath(), o -> {

                                if (cli.optionString(o.getName()) != null) {
                                    return cli.optionString(o.getName());
                                }
                                return o.getDefaultValue();

                            })));

                    List<URL> collect = optionMetadataSet.stream()
                            .filter(o -> o.getConfigResource() != null && cliOpt.options().contains(o.getName()))
                            .map(o -> o.getConfigResource().getUrl())
                            .collect(Collectors.toList());

                    sources.addAll(collect);
                }

                if (!options.isEmpty()) {
                    overrider = overrider.andThen(new InPlaceMapOverrider(options, true, '.'));
                }

                if (!sources.isEmpty()) {
                    overrider = overrider.andThen(new InPlaceFileOverrider(sources, parser, singleConfigMerger));
                }
            }
        }

        return JsonNodeConfigurationBuilder.builder().parser(parser).merger(singleConfigMerger)
                .resources(configurationSource).overrider(overrider).build();
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
