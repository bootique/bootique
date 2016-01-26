package com.nhl.bootique.cli;

import java.io.Writer;
import java.util.Collection;

/**
 * An object that represents a set of command line options passed to Booqtie
 * app.
 * 
 * @since 0.12
 */
public interface Options {

	void printHelp(Writer out);

	boolean hasOption(String name);

	/**
	 * Returns a collection of String values for the specified option name.
	 * 
	 * @param name
	 *            option name
	 * @return a potentially empty collection of CLI values for a given option.
	 */
	Collection<String> stringsFor(String name);
}
