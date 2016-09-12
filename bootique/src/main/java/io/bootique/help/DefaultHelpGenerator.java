package io.bootique.help;

import io.bootique.application.ApplicationMetadata;
import io.bootique.application.OptionMetadata;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Default {@link HelpGenerator} implementation.
 */
public class DefaultHelpGenerator implements HelpGenerator {

    private ApplicationMetadata application;
    private int lineWidth;

    public DefaultHelpGenerator(ApplicationMetadata application, int lineWidth) {
        this.application = application;
        this.lineWidth = lineWidth;
    }

    protected FormattedAppender createAppender(Appendable out) {
        return new FormattedAppender(out, lineWidth);
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
            helpOptions.add(OptionMetadata.builder(c.getName(), c.getDescription()).build());

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


            }

            if (o.isLongNameAllowed()) {

                if(!parts.isEmpty()) {
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
            }


            out.printSubsectionHeader(parts);

            String description = o.getOption().getDescription();
            if (description != null) {
                out.printDescription(description);
            }
        });
    }
}
