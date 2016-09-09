package io.bootique.help;

import io.bootique.cli.meta.CliApplication;
import io.bootique.cli.meta.CliOption;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

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
        printOptions(appender, collectOptions());
    }

    protected Collection<HelpOption> collectOptions() {

        HelpOptions helpOptions = new HelpOptions();

        application.getCommands().forEach(c -> {

            // for now expose commands as simply options (commands are options in a default CLI parser)
            helpOptions.add(CliOption.builder(c.getName(), c.getDescription()).build());

            c.getOptions().forEach(o -> helpOptions.add(o));
        });

        application.getOptions().forEach(o -> helpOptions.add(o));
        return helpOptions.getOptions();
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

    protected void printOptions(FormattedAppender out, Collection<HelpOption> options) {

        if (options.isEmpty()) {
            return;
        }

        out.printSectionName("OPTIONS");
        options.forEach(o -> {

            String valueName = o.getOption().getValueName();
            if (valueName == null || valueName.length() == 0) {
                valueName = "val";
            }

            List<String> parts = new ArrayList<>();
            if (o.isShortNameAllowed()) {
                parts.add("-");
                parts.add(o.getShortName());

                switch (o.getOption().getValueCardinality()) {
                    case REQUIRED:
                        parts.add(" ");
                        parts.add(valueName);
                        break;
                    case OPTIONAL:
                        parts.add(" [");
                        parts.add(valueName);
                        parts.add("]");
                        break;
                }

                parts.add(", ");
            }

            parts.add("--");
            parts.add(o.getOption().getName());
            switch (o.getOption().getValueCardinality()) {
                case REQUIRED:
                    parts.add("=");
                    parts.add(valueName);
                    break;
                case OPTIONAL:
                    parts.add("[=");
                    parts.add(valueName);
                    parts.add("]");
                    break;
            }

            out.printText(parts);

            String description = o.getOption().getDescription();
            if (description != null) {
                out.printDescription(description);
            }
        });
    }
}
