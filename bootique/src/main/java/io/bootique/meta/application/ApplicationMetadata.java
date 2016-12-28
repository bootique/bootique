package io.bootique.meta.application;

import io.bootique.meta.MetadataNode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Metadata object representing current application and its command-line interface.
 *
 * @since 0.20
 */
public class ApplicationMetadata implements MetadataNode {

    private String name;
    private String description;
    private Collection<CommandMetadata> commands;
    private Collection<OptionMetadata> options;

    private ApplicationMetadata() {
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

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public Collection<CommandMetadata> getCommands() {
        return commands;
    }

    public Collection<OptionMetadata> getOptions() {
        return options;
    }

    public static class Builder {

        private ApplicationMetadata application;

        private Builder() {
            this.application = new ApplicationMetadata();
        }

        public ApplicationMetadata build() {
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

        public Builder addCommand(CommandMetadata commandMetadata) {
            application.commands.add(commandMetadata);
            return this;
        }

        public Builder addCommands(Collection<CommandMetadata> commandMetadata) {
            application.commands.addAll(commandMetadata);
            return this;
        }

        public Builder addOption(OptionMetadata option) {
            application.options.add(option);
            return this;
        }

        public Builder addOptions(Collection<OptionMetadata> options) {
            application.options.addAll(options);
            return this;
        }
    }

}
