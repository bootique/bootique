package com.nhl.bootique.jopt;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.swing.JOptionPane;

import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * {@link Cli} implementation on top of {@link JOptionPane} library.
 */
public class JoptCli implements Cli {

	private OptionParser parser;
	private OptionSet optionSet;
	private BootLogger bootLogger;
	private String commandName;

	public JoptCli(BootLogger bootLogger, OptionParser parser, OptionSet parsed, String commandName) {
		this.parser = parser;
		this.optionSet = parsed;
		this.bootLogger = bootLogger;
		this.commandName = commandName;
	}

	@Override
	public String commandName() {
		return commandName;
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
	public List<String> optionStrings(String name) {
		return optionSet.valuesOf(name).stream().map(o -> String.valueOf(o)).collect(toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> standaloneArguments() {
		return (List<String>) optionSet.nonOptionArguments();
	}
}
