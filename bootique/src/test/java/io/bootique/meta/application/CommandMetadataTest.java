package io.bootique.meta.application;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import io.bootique.meta.application.CommandMetadata;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandMetadataTest {

    @Test
    public void testGetShortName() {
        CommandMetadata md = CommandMetadata.builder(MyCommand.class).shortName('M').build();
        assertEquals("my", md.getName());
        assertEquals("M", md.getShortName());
    }

    @Test
    public void testGetName() {
        CommandMetadata md = CommandMetadata.builder(MyCommand.class).build();
        assertEquals("my", md.getName());
        assertEquals("m", md.getShortName());
    }

    @Test
    public void testGetName_CamelCase() {
        CommandMetadata md = CommandMetadata.builder(MyCamelCaseCommand.class).build();
        assertEquals("my-camel-case", md.getName());
        assertEquals("m", md.getShortName());
    }

    @Test
    public void testGetName_UpperCase() {
        CommandMetadata md = CommandMetadata.builder(MYXCommand.class).build();
        assertEquals("myx", md.getName());
        assertEquals("m", md.getShortName());
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
