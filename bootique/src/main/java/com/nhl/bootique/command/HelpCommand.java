package com.nhl.bootique.command;

import java.io.StringWriter;

import com.google.inject.Inject;
import com.nhl.bootique.cli.Cli;
import com.nhl.bootique.log.BootLogger;

public class HelpCommand extends CommandWithMetadata {

	private BootLogger bootLogger;

	@Inject
	public HelpCommand(BootLogger bootLogger) {
		super(CommandMetadata.builder(HelpCommand.class).description("Prints this message.").build());
		this.bootLogger = bootLogger;
	}

	@Override
	public CommandOutcome run(Cli cli) {
		return printHelp(cli);
	}

	protected CommandOutcome printHelp(Cli cli) {
		StringWriter out = new StringWriter();
		cli.printHelp(out);

		bootLogger.stdout(out.toString());
		return CommandOutcome.succeeded();
	}

}
