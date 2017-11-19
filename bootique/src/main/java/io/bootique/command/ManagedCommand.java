package io.bootique.command;

import java.util.Objects;

/**
 * A wrapper around the command instance that provides contextual attributes for that command within Bootique.
 *
 * @since 0.25
 */
public class ManagedCommand {

    private boolean _public;
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

    public boolean isPublic() {
        return _public;
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
            managedCommand._public = true;
            managedCommand._default = false;
            managedCommand.help = false;
        }

        public Builder defaultCommand() {
            managedCommand._default = true;
            return this;
        }

        public Builder privateCommand() {
            managedCommand._public = false;
            return this;
        }

        public Builder helpCommand() {
            managedCommand.help = true;
            return this;
        }

        public ManagedCommand build() {
            return managedCommand;
        }
    }
}
