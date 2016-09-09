package io.bootique.help;

import io.bootique.cli.meta.CliApplication;

import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Default {@link HelpGenerator} implementation.
 */
public class DefaultHelpGenerator implements HelpGenerator {

    private CliApplication application;

    public DefaultHelpGenerator(CliApplication application) {
        this.application = application;
    }

    protected FormattedAppender createAppender(Appendable out) {
        return new FormattedAppender(out);
    }

    @Override
    public void append(Appendable out) {

        FormattedAppender appender = createAppender(out);

        printName(appender, application.getName(), application.getDescription());

        SortedMap<String, String> options = new TreeMap<>();

        application.getCommands().forEach(c -> {

            // for now combine commands and options together (commands are options in a default CLI parser)
            options.put(c.getName(), c.getDescription());

            // TODO: value cardinality, value description
            c.getOptions().forEach(o -> options.put(o.getName(), o.getDescription()));
        });

        application.getOptions().forEach(o -> options.put(o.getName(), o.getDescription()));

        printOptions(appender, options);
    }

    protected void printName(FormattedAppender out, String name, String description) {
        out.printSectionName("NAME");

        Objects.requireNonNull(name);

        if (description != null) {
            out.printText(name, ": ", description);
        } else {
            out.printText(name);
        }
    }

    protected void printOptions(FormattedAppender out, SortedMap<String, String> options) {

        if (options.isEmpty()) {
            return;
        }

        out.printSectionName("OPTIONS");

        options.forEach((name, description) -> {
            out.printText("--", name);

            if (description != null) {
                out.printDescription(description);
            }
        });
    }
}
