package com.nhl.bootique.jopt;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import javax.swing.JOptionPane;

import com.nhl.bootique.cli.Options;
import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * {@link Options} implementation on top of {@link JOptionPane} library.
 */
public class JoptOptions implements Options {

	private OptionParser parser;
	private OptionSet optionSet;
	private BootLogger bootLogger;

	public JoptOptions(BootLogger bootLogger, OptionParser parser, OptionSet parsed) {
		this.parser = parser;
		this.optionSet = parsed;
		this.bootLogger = bootLogger;
	}

	@Override
	public void printHelp(Writer out) {
		try {
			parser.printHelpOn(out);
		} catch (IOException e) {
			bootLogger.stderr("Error printing help", e);
		}
	}

	@Override
	public boolean hasOption(String optionName) {
		return optionSet.has(optionName);
	}

	@Override
	public Collection<String> stringsFor(String optionName) {
		return optionSet.valuesOf(optionName).stream().map(o -> String.valueOf(o)).collect(toList());
	}
}
