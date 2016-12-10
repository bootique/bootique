package io.bootique.application;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandMetadataTest {

    @Test
    public void testGetName() {
        assertEquals("my", CommandMetadata.builder(MyCommand.class).build().getName());
    }

    @Test
    public void testGetName_CamelCase() {
        assertEquals("my-camel-case", CommandMetadata.builder(MyCamelCaseCommand.class).build().getName());
    }

    @Test
    public void testGetName_UpperCase() {
        assertEquals("myx", CommandMetadata.builder(MYXCommand.class).build().getName());
    }

    static class MyCommand implements Command {

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static class MyCamelCaseCommand implements Command {

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static class MYXCommand implements Command {
        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }
}
