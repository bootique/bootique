package com.nhl.bootique.jopt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import com.nhl.bootique.jopt.Options;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class OptionsTest {

	private OptionParser mockParser;
	private OptionSet mockParsed;

	@Before
	public void before() {
		this.mockParser = mock(OptionParser.class);
		this.mockParsed = mock(OptionSet.class);
	}

	@Test
	public void testStringsFor_Missing() {
		
		when(mockParsed.valueOf(anyString())).thenReturn(Collections.emptyList());
		
		Options opts = new Options(mockParser, mockParsed);
		assertNotNull(opts.stringsFor("no_such_opt"));
		assertEquals(0, opts.stringsFor("no_such_opt").size());
	}
}
