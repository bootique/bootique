package com.nhl.launcher.env;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

public class DefaultEnvironment implements Environment {

	private static final String FRAMEWORK_PROPERTIES_PREFIX = "bq";

	@Override
	public String getProperty(String name) {
		return System.getProperty(name);
	}

	@Override
	public Map<String, String> subproperties(String prefix) {

		String lPrefix = prefix.endsWith(".") ? prefix : prefix + ".";
		int len = lPrefix.length();

		return System.getProperties().entrySet().stream().filter(e -> ((String) e.getKey()).startsWith(lPrefix))
				.collect(toMap(e -> ((String) e.getKey()).substring(len), e -> (String) e.getValue()));
	}

	@Override
	public Map<String, String> frameworkProperties() {
		return subproperties(FRAMEWORK_PROPERTIES_PREFIX);
	}
}
