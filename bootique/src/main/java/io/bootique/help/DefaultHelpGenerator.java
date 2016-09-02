package io.bootique.help;

import io.bootique.cli.CliOption;
import io.bootique.command.CommandManager;
import io.bootique.command.CommandMetadata;

import java.io.IOException;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Default {@link HelpGenerator} implementation.
 */
public class DefaultHelpGenerator implements HelpGenerator {

    static final String NEWLINE = System.getProperty("line.separator");

    private static final String OPTIONS_OFFSET = "   ";
    private static final String OPTIONS_DESCRIPTION_OFFSET = OPTIONS_OFFSET + "     ";

    private CommandManager commandManager;
    private Collection<CliOption> standaloneOptions;

    public DefaultHelpGenerator(CommandManager commandManager, Collection<CliOption> standaloneOptions) {
        this.commandManager = commandManager;
        this.standaloneOptions = standaloneOptions;
    }

    @Override
    public void append(Appendable out) {


        SortedMap<String, String> options = new TreeMap<>();

        commandManager.getCommands().forEach(c -> {

            CommandMetadata metadata = c.getMetadata();

            // for now combine commands and options together (commands are options in a default CLI parser)
            options.put(metadata.getName(), metadata.getDescription());

            // TODO: value cardinality, value description
            metadata.getOptions().forEach(o -> options.put(o.getName(), o.getDescription()));

            c.getMetadata().getOptions();
        });

        standaloneOptions.forEach(o -> options.put(o.getName(), o.getDescription()));

        printOptions(out, options);
    }

    protected void printOptions(Appendable out, SortedMap<String, String> options) {

        if (options.isEmpty()) {
            return;
        }

        println(out, "OPTIONS");
        options.forEach((name, description) -> {
            println(out, OPTIONS_OFFSET, "--", name);

            if (description != null) {
                println(out, OPTIONS_DESCRIPTION_OFFSET, description);
            }
        });
    }

    void println(Appendable out, String... lineParts) {
        try {
            for (String p : lineParts) {
                out.append(p);
            }
            out.append(NEWLINE);
        } catch (IOException e) {
            throw new RuntimeException("Error printing help", e);
        }
    }
}
