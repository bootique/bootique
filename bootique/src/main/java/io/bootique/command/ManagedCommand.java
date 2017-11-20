package io.bootique.command;

import java.util.Objects;

/**
 * A wrapper around the command instance that provides contextual attributes for that command within Bootique.
 *
 * @since 0.25
 */
public class ManagedCommand {

    private boolean hidden;
    private boolean _default;
    private boolean help;

    private Command command;

    protected ManagedCommand() {
    }

    public static Builder builder(Command command) {
        return new Builder(command);
    }

    public static ManagedCommand forCommand(Command command) {
        return builder(command).build();
    }

    public Command getCommand() {
        return command;
    }

    public boolean isHidden() {
        return hidden;
    }

    public boolean isDefault() {
        return _default;
    }

    public boolean isHelp() {
        return help;
    }

    public static class Builder {

        private ManagedCommand managedCommand;

        public Builder(Command command) {
            managedCommand = new ManagedCommand();
            managedCommand.command = Objects.requireNonNull(command);
        }

        public Builder asDefault() {
            managedCommand._default = true;
            return this;
        }

        public Builder asHidden() {
            managedCommand.hidden = true;
            return this;
        }

        public Builder asHelp() {
            managedCommand.help = true;
            return this;
        }

        public ManagedCommand build() {
            return managedCommand;
        }
    }
}
