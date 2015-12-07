package com.nhl.launcher.jopt;

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
}
