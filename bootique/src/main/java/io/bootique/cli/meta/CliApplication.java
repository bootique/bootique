package io.bootique.cli.meta;

import io.bootique.Bootique;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

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

    /**
     * Returns application name that is the name of the main class derived from runtime stack.
     *
     * @return application name that is the name of the main class derived from runtime stack.
     */
    static String appNameFromRuntime() {

        String main = mainClass();
        int dot = main.lastIndexOf('.');
        return dot >= 0 && dot < main.length() - 1 ? main.substring(dot + 1) : main;
    }

    /**
     * Returns the name of the app main class. If it can't be found, return 'io.bootique.Bootique'.
     *
     * @return the name of the app main class.
     */
    static String mainClass() {

        for (Map.Entry<Thread, StackTraceElement[]> stackEntry : Thread.getAllStackTraces().entrySet()) {

            // thread must be called "main"
            if ("main".equals(stackEntry.getKey().getName())) {

                StackTraceElement[] stack = stackEntry.getValue();
                StackTraceElement bottom = stack[stack.length - 1];

                // method must be called main
                if ("main".equals(bottom.getMethodName())) {
                    return bottom.getClassName();
                } else {
                    // no other ideas where else to look for main...
                    return Bootique.class.getName();
                }
            }
        }

        // failover...
        return Bootique.class.getName();
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
            return name(CliApplication.appNameFromRuntime());
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
