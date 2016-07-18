package com.nhl.bootique.cli;

import java.io.Writer;
import java.util.List;

/**
 * An object that represents a set of command-line options passed to the
 * Bootique app.
 * 
 * @since 0.12
 */
public interface Cli {

	/**
	 * Returns the name of the command to run, possibly derived from options or
	 * standalone arguments.
	 */
	String commandName();

	// TODO: this probably does not belong here.. instead we should be able to
	// extract all options and print them using external renderer
	void printHelp(Writer out);

	boolean hasOption(String name);

	/**
	 * Returns a List of String values for the specified option name.
	 * 
	 * @param name
	 *            option name
	 * @return a potentially empty collection of CLI values for a given option.
	 */
	List<String> optionStrings(String name);

	/**
	 * Returns a single value for option or null if not present.
	 * 
	 * @param name
	 *            option name.
	 * @return a single value for option or null if not present.
	 * @throws RuntimeException
	 *             if there's more then one value for the option.
	 */
	default String optionString(String name) {
		List<String> allStrings = optionStrings(name);
	
		if (allStrings.size() > 1) {
			throw new RuntimeException("More than one value specified for option: " + name);
		}
	
		return allStrings.isEmpty() ? null : allStrings.get(0);
	}

	/**
	 * Returns all arguments that are not options or option values in the order
	 * they are encountered on the command line.
	 */
	List<String> standaloneArguments();
}
