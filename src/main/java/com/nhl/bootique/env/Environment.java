package com.nhl.bootique.env;

import java.util.Map;

public interface Environment {

	String getProperty(String name);

	/**
	 * Returns all properties in this environment that start with a given prefix
	 * plus a dot separator. The prefix is stripped from the property name in
	 * the Map.
	 */
	Map<String, String> subproperties(String prefix);

	/**
	 * An equivalent to calling {@link #subproperties(String)} with "bq" prefix
	 * argument.
	 */
	Map<String, String> frameworkProperties();
}
