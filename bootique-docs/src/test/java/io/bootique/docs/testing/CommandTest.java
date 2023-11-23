package io.bootique.docs.testing;

import io.bootique.command.CommandOutcome;
import io.bootique.junit5.BQTest;
import io.bootique.junit5.BQTestFactory;
import io.bootique.junit5.BQTestTool;
import io.bootique.junit5.TestIO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@BQTest
public class CommandTest {

    @BQTestTool
    final BQTestFactory testFactory = new BQTestFactory();

    // tag::Testing[]
    @Test
    public void command() {

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
