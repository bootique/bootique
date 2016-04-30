package com.nhl.bootique;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.config.jackson.JsonNodeConfigurationFactory;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.log.DefaultBootLogger;

public class BQCoreModule_ConfigurationFactoryTest {

	private static ConfigurationFactory factory(String... yaml) {
		return factory(Collections.emptyMap(), yaml);
	}

	private static ConfigurationFactory factory(Map<String, String> propertyOverrides, String... yaml) {
		ConfigurationSource mockSource = mock(ConfigurationSource.class);
		when(mockSource.get()).thenReturn(asList(yaml).stream().map(s -> new ByteArrayInputStream(s.getBytes())));

		Environment mockEnvironment = mock(Environment.class);
		when(mockEnvironment.frameworkProperties()).thenReturn(propertyOverrides);

		return factory(mockSource, mockEnvironment);
	}

	private static ConfigurationFactory factory(ConfigurationSource mockSource, Environment mockEnvironment) {

		JacksonService mockJackson = mock(JacksonService.class);
		when(mockJackson.newObjectMapper()).thenReturn(new ObjectMapper());

		ConfigurationFactory factory = BQCoreModule.builder().build().provideConfigurationFactory(mockSource,
				mockEnvironment, mockJackson, new DefaultBootLogger(false));
		assertNotNull(factory);
		assertTrue(factory instanceof JsonNodeConfigurationFactory);

		return factory;
	}

	@Test
	public void testProvideConfigurationFactory_SingleConfig() {

		Bean1 b1 = factory("s: SS\ni: 55").config(Bean1.class, "");
		assertNotNull(b1);
		assertEquals("SS", b1.s);
		assertEquals(55, b1.i);
	}

	@Test
	public void testProvideConfigurationFactory_MultiConfig() {

		Bean1 b1 = factory("s: SS\ni: 55", "l: 12345\ni: 56").config(Bean1.class, "");
		assertNotNull(b1);
		assertEquals("SS", b1.s);
		assertEquals(56, b1.i);
		assertEquals(12345l, b1.l);
	}

	@Test
	public void testProvideConfigurationFactory_SingleConfig_PropOverride() {

		Map<String, String> overrides = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("s", "SS");
				put("i", "55");
			}
		};

		Bean1 b1 = factory(overrides, "s: replace_me\ni: 5").config(Bean1.class, "");
		assertNotNull(b1);
		assertEquals("SS", b1.s);
		assertEquals(55, b1.i);
	}

	@Test
	public void testProvideConfigurationFactory_SingleConfig_PropOverride_Nested() {

		Map<String, String> overrides = new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("b1.s", "SS");
				put("b1.i", "55");
			}
		};

		Bean1 b1 = factory(overrides, "b1:\n  s: replace_me\n  i: 6").config(Bean1.class, "b1");
		assertNotNull(b1);
		assertEquals("SS", b1.s);
		assertEquals(55, b1.i);
	}

	public static class Bean1 {

		private String s;
		private int i;
		private long l;

		public String getS() {
			return s;
		}

		public int getI() {
			return i;
		}

		public long getL() {
			return l;
		}

	}
}
