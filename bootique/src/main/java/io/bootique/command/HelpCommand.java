package io.bootique.command;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.bootique.application.CommandMetadata;
import io.bootique.cli.Cli;
import io.bootique.help.HelpGenerator;
import io.bootique.log.BootLogger;

public class HelpCommand extends CommandWithMetadata {

    private BootLogger bootLogger;
    private Provider<HelpGenerator> helpGeneratorProvider;

    @Inject
    public HelpCommand(BootLogger bootLogger, Provider<HelpGenerator> helpGeneratorProvider) {
        super(CommandMetadata.builder(HelpCommand.class).description("Prints this message.").build());
        this.bootLogger = bootLogger;
        this.helpGeneratorProvider = helpGeneratorProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {
        return printHelp(cli);
    }

    protected CommandOutcome printHelp(Cli cli) {

        StringBuilder out = new StringBuilder();
        helpGeneratorProvider.get().append(out);

        bootLogger.stdout(out.toString());
        return CommandOutcome.succeeded();
    }

}
