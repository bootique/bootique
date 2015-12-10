package com.nhl.bootique.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.factory.YamlFactoryConfigurationService;
import com.nhl.bootique.jackson.JacksonService;

public class YamlFactoryConfigurationServiceTest {

	private ConfigurationSource mockConfigSource;
	private JacksonService mockJacksonService;
	private Environment mockEnvironment;

	@Before
	public void before() {
		mockConfigSource = mock(ConfigurationSource.class);
		mockJacksonService = mock(JacksonService.class);
		when(mockJacksonService.newObjectMapper()).thenReturn(new ObjectMapper());

		mockEnvironment = mock(Environment.class);
	}

	@Test
	public void testCreateFactory() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("s: SS\ni: 55".getBytes());

			return processor.apply(in);
		});

		Bean1 b1 = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment, mockJacksonService)
				.factory(Bean1.class, "");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testCreateFactory_Nested() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("b1:\n  s: SS\n  i: 55".getBytes());

			return processor.apply(in);
		});

		Bean2 b2 = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment, mockJacksonService)
				.factory(Bean2.class, "");
		assertNotNull(b2);
		assertNotNull(b2.getB1());
		assertEquals("SS", b2.getB1().getS());
		assertEquals(55, b2.getB1().getI());
	}

	@Test
	public void testCreateFactory_Subconfig() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("b1:\n  s: SS\n  i: 55".getBytes());

			return processor.apply(in);
		});

		Bean1 b1 = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment, mockJacksonService)
				.factory(Bean1.class, "b1");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testCreateFactory_Subconfig_MultiLevel() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("b0:\n  b1:\n    s: SS\n    i: 55".getBytes());

			return processor.apply(in);
		});

		Bean1 b1 = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment, mockJacksonService)
				.factory(Bean1.class, "b0.b1");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testCreateFactory_Subconfig_Missing() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("b1:\n  s: SS\n  i: 55".getBytes());

			return processor.apply(in);
		});

		Bean1 b1 = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment, mockJacksonService)
				.factory(Bean1.class, "no.such.path");
		assertNotNull(b1);
		assertEquals(null, b1.getS());
		assertEquals(0, b1.getI());
	}

	@Test
	public void testCreateFactory_PropSubstitution() {

		when(mockEnvironment.frameworkProperties()).thenReturn(new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put("s", "SS");
				put("i", "55");
			}
		});

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("s: replace_me\ni: replace_me".getBytes());

			return processor.apply(in);
		});

		Bean1 b1 = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment, mockJacksonService)
				.factory(Bean1.class, "");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testCreateFactory_PropSubstitution_Nested() {

		when(mockEnvironment.frameworkProperties()).thenReturn(new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put("b1.s", "SS");
				put("b1.i", "55");
			}
		});

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("b1:\n  s: replace_me\n  i: replace_me".getBytes());

			return processor.apply(in);
		});

		Bean1 b1 = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment, mockJacksonService)
				.factory(Bean1.class, "b1");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());

	}

	public static class Bean1 {

		private String s;
		private int i;

		public String getS() {
			return s;
		}

		public void setS(String s) {
			this.s = s;
		}

		public int getI() {
			return i;
		}

		public void setI(int i) {
			this.i = i;
		}
	}

	public static class Bean2 {

		private Bean1 b1;

		public Bean1 getB1() {
			return b1;
		}

		public void setB1(Bean1 b1) {
			this.b1 = b1;
		}
	}

}
