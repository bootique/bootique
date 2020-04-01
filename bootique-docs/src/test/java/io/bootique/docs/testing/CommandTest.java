package io.bootique.docs.testing;

import io.bootique.command.CommandOutcome;
import io.bootique.test.TestIO;
import io.bootique.test.junit5.BQTestFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandTest {

    @RegisterExtension
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
