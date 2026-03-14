package io.bootique.docs.programming.commands.decorating;

import io.bootique.BQCoreModule;
import io.bootique.command.CommandDecorator;
import io.bootique.BQModule;
import io.bootique.di.Binder;

public class MyCommand implements BQModule {
    // tag::Commands[]
    CommandDecorator extraCommands = CommandDecorator.builder()
            .beforeRun(CustomHealthcheckCommand.class)
            .alsoRun(ScheduleCommand.class)
            .alsoRun(HeartbeatCommand.class)
            .build();

    // end::Commands[]

    @Override
    public void configure(Binder binder) {
        // tag::Commands[]
        BQCoreModule.extend(binder).decorateCommand(ServerCommand.class, extraCommands);
        // end::Commands[]
    }
}
