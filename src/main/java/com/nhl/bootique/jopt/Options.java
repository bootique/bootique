package com.nhl.bootique.jopt;

import static java.util.stream.Collectors.toList;

import java.util.Collection;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Options {

	private OptionParser parser;
	private OptionSet optionSet;

	public Options(OptionParser parser, OptionSet parsed) {
		this.parser = parser;
		this.optionSet = parsed;
	}

	public OptionParser getParser() {
		return parser;
	}

	public OptionSet getOptionSet() {
		return optionSet;
	}

	public Collection<String> stringsFor(String optionName) {
		return optionSet.valuesOf(optionName).stream().map(o -> String.valueOf(o)).collect(toList());
	}
}
