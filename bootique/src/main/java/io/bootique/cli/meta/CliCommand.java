package io.bootique.cli.meta;

import io.bootique.command.Command;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 0.20
 */
public class CliCommand extends CliNode {

    private Collection<CliOption> options;

    public CliCommand() {
        this.options = new ArrayList<>();
    }

    public static Builder builder(Class<? extends Command> commandType) {
        return new Builder().commandType(commandType);
    }

    public static Builder builder(String commandName) {
        return new Builder().name(commandName);
    }

    public Collection<CliOption> getOptions() {
        return options;
    }

    public static class Builder {

        private CliCommand command;

        private Builder() {
            this.command = new CliCommand();
        }

        public CliCommand build() {
            return command;
        }

        public Builder commandType(Class<? extends Command> commandType) {
            command.name = defaultName(commandType);
            return this;
        }

        public Builder name(String name) {
            command.name = name;
            return this;
        }

        public Builder description(String description) {
            this.command.description = description;
            return this;
        }

        public Builder addOption(CliOption option) {
            this.command.options.add(option);
            return this;
        }

        public Builder addOptions(Collection<CliOption> options) {
            this.command.options.addAll(options);
            return this;
        }

        public Builder addOption(CliOption.Builder optionBuilder) {
            return addOption(optionBuilder.build());
        }

        // TODO: copy/paste from ConfigModule... reuse somehow...
        private String defaultName(Class<? extends Command> commandType) {
            String name = commandType.getSimpleName().toLowerCase();
            final String stripSuffix = "command";
            return (name.endsWith(stripSuffix)) ? name.substring(0, name.length() - stripSuffix.length()) : name;
        }
    }
}
