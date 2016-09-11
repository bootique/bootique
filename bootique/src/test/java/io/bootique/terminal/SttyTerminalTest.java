package io.bootique.terminal;

import io.bootique.log.BootLogger;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

public class SttyTerminalTest {

    private SttyTerminal terminal;

    @Before
    public void before() {
        terminal = new SttyTerminal(mock(BootLogger.class));
    }

    @Test
    public void testParseLine_OSX() {
        String line = "speed 9600 baud; 39 rows; 136 columns;";
        assertEquals(new Integer(136), terminal.parseLine(line));
    }

    @Test
    public void testParseColumns_OSX() {
        String line = "speed 9600 baud; 39 rows; 136 columns;";
        BufferedReader in = new BufferedReader(new StringReader(line));
        assertEquals(new Integer(136), terminal.parseColumns(in));
    }

    @Test
    public void testParseLine_Linux() {
        // from Centos 7:
        String line = "speed 9600 baud; rows 40; columns 148; line = 0;";
        assertEquals(new Integer(148), terminal.parseLine(line));
    }

    @Test
    public void testParseLine_Docker_Ubuntu() {

        // docker  run -t ubuntu:latest stty -a
        // same as any lunux, except the value is 0

        String line = "speed 38400 baud; rows 0; columns 0; line = 0;";
        assertEquals(new Integer(0), terminal.parseLine(line));
    }

    @Test
    public void testParseLine_Invalid() {

        String line = "not a valid line";
        assertNull(terminal.parseLine(line));
    }
}
