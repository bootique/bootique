package com.nhl.bootique.factory;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.google.inject.Inject;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.jackson.JacksonService;

/**
 * {@link FactoryConfigurationService} based on YAML configs.
 */
public class YamlFactoryConfigurationService implements FactoryConfigurationService {

	private JsonNode rootNode;
	private ObjectMapper mapper;

	protected static JsonNode readYaml(InputStream in, ObjectMapper mapper) {
		try {
			YAMLParser parser = new YAMLFactory().createParser(in);
			return mapper.readTree(parser);
		} catch (IOException e) {
			throw new RuntimeException("Error reading config file", e);
		}
	}

	@Inject
	public YamlFactoryConfigurationService(ConfigurationSource configurationSource, Environment environment,
			JacksonService jacksonService) {
		this.mapper = jacksonService.newObjectMapper();
		this.rootNode = configurationSource.readConfig(in -> readYaml(in, mapper));

		JsonPropertiesResolver.resolve(rootNode, environment.frameworkProperties());
	}

	@Override
	public <T> T factory(Class<T> type, String prefix) {

		// allow no-config state
		JsonNode rootNode = (this.rootNode == null) ? new ObjectNode(null) : this.rootNode;

		return subconfig(rootNode, prefix, type);
	}

	protected <T> T subconfig(JsonNode node, String prefix, Class<T> type) {

		node = JsonPropertiesResolver.findChild(node, prefix);

		try {
			return mapper.readValue(new TreeTraversingParser(node), type);
		}
		// TODO: implement better exception handling. See ConfigurationFactory
		// in Dropwizard for inspiration
		catch (IOException e) {
			throw new RuntimeException("Error creating config", e);
		}
	}

}
