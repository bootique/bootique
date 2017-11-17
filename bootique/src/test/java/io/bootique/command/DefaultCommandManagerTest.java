package io.bootique.command;

import io.bootique.cli.Cli;
import io.bootique.meta.application.CommandMetadata;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertSame;

public class DefaultCommandManagerTest {

    private Command c1 = new C1();
    private Command c2 = new C2();
    private Command c3 = new C3();

    @Test
    public void testLookupByType() {

        Map<String, Command> map = new HashMap<>();
        map.put("x", c1);
        map.put("y", c2);

        DefaultCommandManager cm = new DefaultCommandManager(map, Optional.of(c3), Optional.of(c1));
        assertSame(c1, cm.lookupByType(C1.class));
        assertSame(c2, cm.lookupByType(C2.class));
        assertSame("No default command included in lookup", c3, cm.lookupByType(C3.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLookupByType_Missing() {

        Map<String, Command> map = new HashMap<>();
        map.put("x", c1);

        DefaultCommandManager cm = new DefaultCommandManager(map, Optional.empty(), Optional.empty());
        cm.lookupByType(C2.class);
    }


    @Test
    public void testLookupByName() {

        Map<String, Command> map = new HashMap<>();
        map.put("c1", c1);
        map.put("c2", c2);

        DefaultCommandManager cm = new DefaultCommandManager(map, Optional.of(c3), Optional.of(c1));
        assertSame(c1, cm.lookupByName("c1"));
        assertSame(c2, cm.lookupByName("c2"));
        assertSame("No default command included in lookup", c3, cm.lookupByName("c3"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLookupByName_Missing() {

        Map<String, Command> map = new HashMap<>();
        map.put("c1", c1);

        DefaultCommandManager cm = new DefaultCommandManager(map, Optional.empty(), Optional.empty());
        cm.lookupByName("c2");
    }

    static class C1 extends CommandWithMetadata {
        public C1() {
            super(CommandMetadata.builder(C1.class).build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static class C2 extends CommandWithMetadata {
        public C2() {
            super(CommandMetadata.builder(C2.class).build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static class C3 extends CommandWithMetadata {
        public C3() {
            super(CommandMetadata.builder(C3.class).build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }
}
