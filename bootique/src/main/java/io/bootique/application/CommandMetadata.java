package io.bootique.application;

import io.bootique.command.Command;
import io.bootique.names.ClassToName;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 0.20
 */
public class CommandMetadata extends ApplicationMetadataNode {

    private String shortName;
    private Collection<OptionMetadata> options;

    public CommandMetadata() {
        this.options = new ArrayList<>();
    }

    public static Builder builder(Class<? extends Command> commandType) {
        return new Builder().commandType(commandType);
    }

    public static Builder builder(String commandName) {
        return new Builder().name(commandName);
    }

    public Collection<OptionMetadata> getOptions() {
        return options;
    }

    /**
     * @since 0.21
     * @return command short name.
     */
    public String getShortName() {
        return (shortName != null) ? shortName : name.substring(0, 1);
    }

    public static class Builder {

        private static ClassToName NAME_BUILDER = ClassToName
                .builder()
                .convertToLowerCase()
                .partsSeparator("-")
                .stripSuffix("Command")
                .build();

        private CommandMetadata command;

        private Builder() {
            this.command = new CommandMetadata();
        }

        public CommandMetadata build() {
            validateName(command.name);
            return command;
        }

        public Builder commandType(Class<? extends Command> commandType) {
            command.name = NAME_BUILDER.toName(commandType);
            return this;
        }

        public Builder name(String name) {
            command.name = validateName(name);
            return this;
        }

        public Builder shortName(char shortName) {
            command.shortName = String.valueOf(shortName);
            return this;
        }

        public Builder description(String description) {
            this.command.description = description;
            return this;
        }

        public Builder addOption(OptionMetadata option) {
            this.command.options.add(option);
            return this;
        }

        public Builder addOptions(Collection<OptionMetadata> options) {
            this.command.options.addAll(options);
            return this;
        }

        public Builder addOption(OptionMetadata.Builder optionBuilder) {
            return addOption(optionBuilder.build());
        }

        private String validateName(String name) {
            if(name == null) {
                throw new IllegalArgumentException("Null 'name'");
            }

            if(name.length() == 0) {
                throw new IllegalArgumentException("Empty 'name'");
            }

            return name;
        }
    }
}
