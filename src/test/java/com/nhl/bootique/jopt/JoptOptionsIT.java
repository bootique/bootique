package com.nhl.bootique.jopt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import com.nhl.bootique.log.BootLogger;

import joptsimple.OptionParser;

public class JoptOptionsIT {

	private OptionParser parser;
	private BootLogger mockBootLogger;
	private JoptOptionsBuilder optionsBuilder;

	@Before
	public void before() {
		this.parser = new OptionParser();
		this.mockBootLogger = mock(BootLogger.class);
		this.optionsBuilder = new JoptOptionsBuilder(parser, mockBootLogger);
	}

	@Test
	public void testHasOption() {
		optionsBuilder.add("me", null);

		assertTrue(createOptions("-m").hasOption("me"));
		assertTrue(createOptions("--me").hasOption("me"));
		assertFalse(createOptions("-m").hasOption("not_me"));
		assertFalse(createOptions("-m").hasOption("m"));
	}

	@Test
	public void testOptionStrings_Short() {
		optionsBuilder.add("me", null).mayTakeArgument(null);

		assertEquals(createOptions("-m v4").optionStrings("me"), "v4");
	}

	@Test
	public void testOptionStrings_Long_Equals() {
		optionsBuilder.add("me", null).mayTakeArgument(null);

		assertEquals(createOptions("--me=v4").optionStrings("me"), "v4");
	}

	@Test
	public void testOptionStrings_Long_Space() {
		optionsBuilder.add("me", null).mayTakeArgument(null);

		assertEquals(createOptions("--me v4").optionStrings("me"), "v4");
	}

	@Test
	public void testOptionStrings_Single_Mixed() {
		optionsBuilder.add("me", null).mayTakeArgument(null);
		optionsBuilder.add("other", null).mayTakeArgument(null);

		assertEquals(createOptions("--other v2 --me=v4").optionStrings("me"), "v4");
	}

	@Test
	public void testOptionStrings_Multiple_Mixed() {
		optionsBuilder.add("me", null).mayTakeArgument(null);
		optionsBuilder.add("other", null).mayTakeArgument(null);
		optionsBuilder.add("n", null).mayTakeArgument(null);
		optionsBuilder.add("yes", null);

		assertEquals(createOptions("--me=v1 --other v2 -n v3 --me v4 --yes").optionStrings("me"), "v1", "v4");
	}

	@Test
	public void testNonOptionArgs_Mix() {
		optionsBuilder.add("me", null).mayTakeArgument(null);
		optionsBuilder.add("other", null).mayTakeArgument(null);
		optionsBuilder.add("yes", null);

		assertEquals(createOptions("a --me=v1 --other v2 b --me v4 --yes c d").nonOptionArgs(), "a", "b", "c", "d");
	}

	@Test
	public void testNonOptionArgs_DashDash() {
		optionsBuilder.add("me", null).mayTakeArgument(null);
		optionsBuilder.add("other", null).mayTakeArgument(null);

		assertEquals(createOptions("a --me=v1 -- --other v2").nonOptionArgs(), "a", "--other", "v2");
	}

	private void assertEquals(Collection<String> result, String... expected) {
		assertArrayEquals(expected, result.toArray());
	}

	private JoptOptions createOptions(String args) {
		String[] argsArray = args.split(" ");
		return optionsBuilder.build(argsArray);
	}
}
