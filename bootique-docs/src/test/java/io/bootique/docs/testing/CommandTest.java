package io.bootique.docs.testing;

import io.bootique.command.CommandOutcome;
import io.bootique.test.TestIO;
import io.bootique.test.junit.BQTestFactory;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandTest {

    @Rule
    public BQTestFactory testFactory = new BQTestFactory();

    // tag::Testing[]
    @Test
    public void testCommand() {

        TestIO io = TestIO.noTrace();
        CommandOutcome outcome = testFactory
                .app("--help")
                .bootLogger(io.getBootLogger())
                .run();

        assertEquals(0, outcome.getExitCode());
        assertTrue(io.getStdout().contains("--help"));
        assertTrue(io.getStdout().contains("--config"));
    }
    // end::Testing[]

}
