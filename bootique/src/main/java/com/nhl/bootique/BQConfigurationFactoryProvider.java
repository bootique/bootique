package com.nhl.bootique;

import java.io.InputStream;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.config.jackson.InPlaceLeftHandMerger;
import com.nhl.bootique.config.jackson.InPlaceMapOverrider;
import com.nhl.bootique.config.jackson.JsonNodeConfigurationBuilder;
import com.nhl.bootique.config.jackson.JsonNodeConfigurationFactory;
import com.nhl.bootique.config.jackson.JsonNodeYamlParser;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.log.BootLogger;

/**
 * @since 0.17
 */
public class BQConfigurationFactoryProvider implements Provider<ConfigurationFactory> {

	private ConfigurationSource configurationSource;
	private Environment environment;
	private JacksonService jacksonService;
	private BootLogger bootLogger;

	@Inject
	public BQConfigurationFactoryProvider(ConfigurationSource configurationSource, Environment environment,
			JacksonService jacksonService, BootLogger bootLogger) {

		this.configurationSource = configurationSource;
		this.environment = environment;
		this.jacksonService = jacksonService;
		this.bootLogger = bootLogger;
	}

	@Override
	public ConfigurationFactory get() {

		Function<InputStream, JsonNode> parser = new JsonNodeYamlParser(jacksonService.newObjectMapper());
		BinaryOperator<JsonNode> singleConfigMerger = new InPlaceLeftHandMerger(bootLogger);
		Function<JsonNode, JsonNode> overrider = new InPlaceMapOverrider(environment.frameworkProperties(), true,
				'.');

		Map<String, String> vars = environment.frameworkVariables();

		ObjectMapper jsonToObjectMapper = jacksonService.newObjectMapper();

		if (!vars.isEmpty()) {

			// switching to slower CI strategy for mapping properties...
			jsonToObjectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);

			overrider = overrider.andThen(new InPlaceMapOverrider(vars, false, '_'));
		}

		JsonNode rootNode = JsonNodeConfigurationBuilder.builder().parser(parser).merger(singleConfigMerger)
				.resources(configurationSource).overrider(overrider).build();

		return new JsonNodeConfigurationFactory(rootNode, jsonToObjectMapper);
	}
}
