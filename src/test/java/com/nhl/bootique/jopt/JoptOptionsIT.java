package com.nhl.bootique.jopt;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

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
	public void testStringsFor_Short() {
		optionsBuilder.add("me", null).mayTakeArgument(null);

		assertArrayEquals(new String[] { "v4" }, createOptions("-m v4").stringsFor("me").toArray());
	}

	@Test
	public void testStringsFor_Long_Equals() {
		optionsBuilder.add("me", null).mayTakeArgument(null);

		assertArrayEquals(new String[] { "v4" }, createOptions("--me=v4").stringsFor("me").toArray());
	}

	@Test
	public void testStringsFor_Long_Space() {
		optionsBuilder.add("me", null).mayTakeArgument(null);

		assertArrayEquals(new String[] { "v4" }, createOptions("--me v4").stringsFor("me").toArray());
	}

	@Test
	public void testStringsFor_Single_Mixed() {
		optionsBuilder.add("me", null).mayTakeArgument(null);
		optionsBuilder.add("other", null).mayTakeArgument(null);

		assertArrayEquals(new String[] { "v4" }, createOptions("--other v2 --me=v4").stringsFor("me").toArray());
	}

	@Test
	public void testStringsFor_Multiple_Mixed() {
		optionsBuilder.add("me", null).mayTakeArgument(null);
		optionsBuilder.add("other", null).mayTakeArgument(null);
		optionsBuilder.add("n", null).mayTakeArgument(null);
		optionsBuilder.add("yes", null);

		assertArrayEquals(new String[] { "v1", "v4" },
				createOptions("--me=v1 --other v2 -n v3 --me v4 --yes").stringsFor("me").toArray());
	}

	protected JoptOptions createOptions(String args) {
		String[] argsArray = args.split(" ");
		return optionsBuilder.build(argsArray);
	}

}
