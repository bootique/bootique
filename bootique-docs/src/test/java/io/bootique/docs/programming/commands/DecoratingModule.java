package io.bootique.docs.programming.commands;

import io.bootique.BQCoreModule;
import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandDecorator;
import io.bootique.BQModule;
import io.bootique.command.CommandOutcome;
import io.bootique.di.Binder;

public class DecoratingModule implements BQModule {

    // tag::decorate[]
    @Override
    public void configure(Binder binder) {

        CommandDecorator extraCommands = CommandDecorator.builder()
                .beforeRun(MyHealthcheckCommand.class)
                .alsoRun(ScheduleCommand.class)
                .alsoRun(HeartbeatCommand.class)
                .build();

        BQCoreModule.extend(binder).decorateCommand(ServerCommand.class, extraCommands);
    }
    // end::decorate[]

    static class ServerCommand implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return null;
        }
    }

    static class HeartbeatCommand implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return null;
        }
    }

    static class MyHealthcheckCommand implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return null;
        }
    }

    static class ScheduleCommand implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return null;
        }
    }
}
