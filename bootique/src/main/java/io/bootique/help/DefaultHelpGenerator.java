package io.bootique.help;

import io.bootique.cli.meta.CliApplication;

import java.io.IOException;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Default {@link HelpGenerator} implementation.
 */
public class DefaultHelpGenerator implements HelpGenerator {

    static final String NEWLINE = System.getProperty("line.separator");

    private static final String TEXT_OFFSET = "   ";
    private static final String DESCRIPTION_OFFSET = TEXT_OFFSET + "     ";

    private CliApplication application;

    public DefaultHelpGenerator(CliApplication application) {
        this.application = application;
    }

    @Override
    public void append(Appendable out) {

        printName(out, application.getName(), application.getDescription());

        SortedMap<String, String> options = new TreeMap<>();

        application.getCommands().forEach(c -> {

            // for now combine commands and options together (commands are options in a default CLI parser)
            options.put(c.getName(), c.getDescription());

            // TODO: value cardinality, value description
            c.getOptions().forEach(o -> options.put(o.getName(), o.getDescription()));
        });

        application.getOptions().forEach(o -> options.put(o.getName(), o.getDescription()));

        printOptions(out, options);
    }

    protected void printName(Appendable out, String name, String description) {
        println(out, "NAME");

        Objects.requireNonNull(name);

        if(description != null) {
            println(out, TEXT_OFFSET, name, ": ", description);
        }
        else {
            println(out, TEXT_OFFSET, name);
        }
    }

    protected void printOptions(Appendable out, SortedMap<String, String> options) {

        if (options.isEmpty()) {
            return;
        }

        println(out, "OPTIONS");
        options.forEach((name, description) -> {
            println(out, TEXT_OFFSET, "--", name);

            if (description != null) {
                println(out, DESCRIPTION_OFFSET, description);
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
