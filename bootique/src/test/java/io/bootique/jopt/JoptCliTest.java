package io.bootique.jopt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import io.bootique.jopt.JoptCli;
import org.junit.Before;
import org.junit.Test;

import io.bootique.log.BootLogger;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class JoptCliTest {

	private OptionParser mockParser;
	private OptionSet mockParsed;
	private BootLogger mockBootLogger;

	@Before
	public void before() {
		this.mockParser = mock(OptionParser.class);
		this.mockParsed = mock(OptionSet.class);
		this.mockBootLogger = mock(BootLogger.class);
	}

	@Test
	public void testStringsFor_Missing() {

		when(mockParsed.valueOf(anyString())).thenReturn(Collections.emptyList());

		JoptCli opts = new JoptCli(mockBootLogger, mockParser, mockParsed, "aname");
		assertNotNull(opts.optionStrings("no_such_opt"));
		assertEquals(0, opts.optionStrings("no_such_opt").size());
	}

	@Test
	public void testCommandName() {
		JoptCli o1 = new JoptCli(mockBootLogger, mockParser, mockParsed, "aname");
		assertEquals("aname", o1.commandName());

		JoptCli o2 = new JoptCli(mockBootLogger, mockParser, mockParsed, null);
		assertNull(o2.commandName());
	}
}
