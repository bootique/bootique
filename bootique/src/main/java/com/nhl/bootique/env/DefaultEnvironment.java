package com.nhl.bootique.env;

import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;

/**
 * An {@link Environment} implementation that reads properties from the Map
 * passed on constructor.
 */
public class DefaultEnvironment implements Environment {

	/**
	 * If present, enables boot sequence tracing to STDERR.
	 */
	public static final String TRACE_PROPERTY = "bq.trace";

	private Map<String, String> properties;
	private Map<String, String> variables;

	public DefaultEnvironment(Map<String, String> diProperties, Map<String, String> diVariables) {
		this.properties = new HashMap<>(diProperties);
		this.variables = new HashMap<>(diVariables);

		// override DI props from system...
		System.getProperties().forEach((k, v) -> properties.put((String) k, (String) v));
		System.getenv().forEach((k, v) -> variables.put(k, v));
	}

	@Override
	public String getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public Map<String, String> subproperties(String prefix) {
		return filterByPrefix(properties, prefix, ".");
	}

	@Override
	public String getVariable(String name) {
		return variables.get(name);
	}

	@Override
	public Map<String, String> variables(String prefix) {
		return filterByPrefix(variables, prefix, "_");
	}

	protected Map<String, String> filterByPrefix(Map<String, String> unfiltered, String prefix, String separator) {
		String lPrefix = prefix.endsWith(separator) ? prefix : prefix + separator;
		int len = lPrefix.length();

		return unfiltered.entrySet().stream().filter(e -> e.getKey().startsWith(lPrefix))
				.collect(toMap(e -> e.getKey().substring(len), e -> e.getValue()));
	}
}
