package io.bootique.cli.meta;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Metadata for the app application command invocation structure.
 *
 * @since 0.20
 */
public class CliApplication extends CliNode {

    private Collection<CliCommand> commands;
    private Collection<CliOption> options;

    private CliApplication() {
        this.commands = new ArrayList<>();
        this.options = new ArrayList<>();
    }

    public static Builder builder() {
        return new Builder().defaultName();
    }

    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    public static Builder builder(String name, String description) {
        return new Builder().name(name).description(description);
    }


    public Collection<CliCommand> getCommands() {
        return commands;
    }

    public Collection<CliOption> getOptions() {
        return options;
    }

    public static class Builder {

        private CliApplication application;

        private Builder() {
            this.application = new CliApplication();
        }

        public CliApplication build() {
            return application;
        }

        public Builder name(String name) {
            application.name = name;
            return this;
        }

        public Builder defaultName() {
            return name(ApplicationIntrospector.appNameFromRuntime());
        }

        public Builder description(String description) {
            application.description = description;
            return this;
        }

        public Builder addCommand(CliCommand commandMetadata) {
            application.commands.add(commandMetadata);
            return this;
        }

        public Builder addCommands(Collection<CliCommand> commandMetadata) {
            application.commands.addAll(commandMetadata);
            return this;
        }

        public Builder addOption(CliOption option) {
            application.options.add(option);
            return this;
        }

        public Builder addOptions(Collection<CliOption> options) {
            application.options.addAll(options);
            return this;
        }
    }

}
