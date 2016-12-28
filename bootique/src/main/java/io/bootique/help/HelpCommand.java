package io.bootique.help;

import com.google.inject.Provider;
import io.bootique.meta.application.CommandMetadata;
import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.log.BootLogger;

/**
 * @since 0.21 moved to io.bootique.help
 */
public class HelpCommand extends CommandWithMetadata {

    private BootLogger bootLogger;
    private Provider<HelpGenerator> helpGeneratorProvider;

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
