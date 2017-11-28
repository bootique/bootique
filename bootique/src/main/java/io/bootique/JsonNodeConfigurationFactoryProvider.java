package io.bootique;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.cli.Cli;
import io.bootique.config.ConfigurationFactory;
import io.bootique.config.ConfigurationSource;
import io.bootique.config.OptionRefWithConfig;
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
import java.util.Collections;
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
    private Set<OptionMetadata> optionMetadata;
    private Set<OptionRefWithConfig> optionDecorators;
    private Cli cli;

    @Inject
    public JsonNodeConfigurationFactoryProvider(
            ConfigurationSource configurationSource,
            Environment environment,
            JacksonService jacksonService,
            BootLogger bootLogger,
            Set<OptionMetadata> optionMetadata,
            Set<OptionRefWithConfig> optionDecorators,
            Cli cli) {

        this.configurationSource = configurationSource;
        this.environment = environment;
        this.jacksonService = jacksonService;
        this.bootLogger = bootLogger;
        this.optionMetadata = optionMetadata;
        this.optionDecorators = optionDecorators;
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

        if (optionMetadata.isEmpty()) {
            return overrider;
        }

        List<OptionSpec<?>> detectedOptions = cli.detectedOptions();
        if (detectedOptions.isEmpty()) {
            return overrider;
        }

        List<URL> decoratorSources = optionDecorators.isEmpty() ? Collections.emptyList() : new ArrayList<>(5);

        // options tied to config property paths
        HashMap<String, String> options = new HashMap<>(5);

        // options tied to config resources
        List<URL> optionSources = new ArrayList<>(5);

        for (OptionSpec<?> cliOpt : detectedOptions) {

            OptionMetadata omd = findMetadata(cliOpt);

            if (omd == null) {
                continue;
            }

            for (OptionRefWithConfig decorator : optionDecorators) {
                if (decorator.getOptionName().equals(omd.getName())) {
                    decoratorSources.add(decorator.getConfigResource().getUrl());
                }
            }

            if (omd.getConfigPath() != null) {
                String cliValue = cli.optionString(omd.getName());
                if (cliValue == null) {
                    cliValue = omd.getDefaultValue();
                }

                options.put(omd.getConfigPath(), cliValue);
            }

            if (omd.getConfigResource() != null) {
                optionSources.add(omd.getConfigResource().getUrl());
            }
        }

        // config decorators are loaded first, and then can be overridden from options...
        if(!decoratorSources.isEmpty()) {
            overrider = overrider.andThen(new InPlaceResourceOverrider(decoratorSources, parser, singleConfigMerger));
        }

        if (!options.isEmpty()) {
            overrider = overrider.andThen(new InPlaceMapOverrider(options, true, '.'));
        }

        // deprecated...
        if (!optionSources.isEmpty()) {
            overrider = overrider.andThen(new InPlaceResourceOverrider(optionSources, parser, singleConfigMerger));
        }

        return overrider;
    }

    private OptionMetadata findMetadata(OptionSpec<?> option) {

        List<String> optionNames = option.options();

        // TODO: allow lookup of option metadata by name to avoid linear scans...
        // Though we are dealing with small collection, so shouldn't be too horrible.

        for (OptionMetadata omd : optionMetadata) {
            if (optionNames.contains(omd.getName())) {
                return omd;
            }
        }

        // this was likely a command, not an option.
        return null;
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
