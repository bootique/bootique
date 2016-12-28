package io.bootique.help.config;

import com.google.inject.Provider;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;

public class HelpConfigCommand extends CommandWithMetadata {

    private BootLogger bootLogger;
    private Provider<ConfigHelpGenerator> helpGeneratorProvider;

    public HelpConfigCommand(BootLogger bootLogger, Provider<ConfigHelpGenerator> helpGeneratorProvider) {
        super(CommandMetadata
                .builder(HelpConfigCommand.class)
                .description("Prints information about application modules and their configuration options.")
                .shortName('H'));

        this.bootLogger = bootLogger;
        this.helpGeneratorProvider = helpGeneratorProvider;
    }

    @Override
    public CommandOutcome run(Cli cli) {

        StringBuilder out = new StringBuilder();
        helpGeneratorProvider.get().append(out);
        bootLogger.stdout(out.toString());

        return CommandOutcome.succeeded();
    }
}
