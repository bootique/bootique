package com.nhl.bootique.jopt;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.swing.JOptionPane;

import com.nhl.bootique.cli.CommandLine;
import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * {@link CommandLine} implementation on top of {@link JOptionPane} library.
 */
public class JoptOptions implements CommandLine {

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
	public List<String> optionStrings(String optionName) {
		return optionSet.valuesOf(optionName).stream().map(o -> String.valueOf(o)).collect(toList());
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<String> standaloneArguments() {
		return (List<String>) optionSet.nonOptionArguments();
	}
}
