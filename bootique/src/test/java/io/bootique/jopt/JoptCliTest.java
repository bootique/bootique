package io.bootique.jopt;

import joptsimple.OptionSet;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JoptCliTest {

	private OptionSet mockParsed;

	@Before
	public void before() {
		this.mockParsed = mock(OptionSet.class);
	}

	@Test
	public void testStringsFor_Missing() {

		when(mockParsed.valueOf(anyString())).thenReturn(Collections.emptyList());

		JoptCli opts = new JoptCli(mockParsed, "aname");
		assertNotNull(opts.optionStrings("no_such_opt"));
		assertEquals(0, opts.optionStrings("no_such_opt").size());
	}

	@Test
	public void testCommandName() {
		JoptCli o1 = new JoptCli(mockParsed, "aname");
		assertEquals("aname", o1.commandName());

		JoptCli o2 = new JoptCli(mockParsed, null);
		assertNull(o2.commandName());
	}
}
