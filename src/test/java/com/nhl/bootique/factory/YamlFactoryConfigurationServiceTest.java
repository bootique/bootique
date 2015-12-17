package com.nhl.bootique.factory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.bootique.config.ConfigurationSource;
import com.nhl.bootique.env.Environment;
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
	public void testFactory() {

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
	public void testFactory_Nested() {

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
	public void testFactory_Subconfig() {

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
	public void testFactory_Subconfig_MultiLevel() {

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
	public void testFactory_Subconfig_Missing() {

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
	public void testFactory_PropSubstitution() {

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
	public void testFactory_PropSubstitution_Nested() {

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

	@Test
	public void testList_SingleLevel() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("- SS\n- 55".getBytes());

			return processor.apply(in);
		});

		List<Object> l = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment, mockJacksonService)
				.factory(new TypeReference<List<Object>>() {
				}, "");

		assertNotNull(l);
		assertEquals("SS", l.get(0));
		assertEquals(55, l.get(1));
	}

	@Test
	public void testList_MultiLevel() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("-\n  - SS\n  - 55\n-\n  - X".getBytes());

			return processor.apply(in);
		});

		List<List<Object>> l = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment,
				mockJacksonService).factory(new TypeReference<List<List<Object>>>() {
				}, "");

		assertNotNull(l);
		assertEquals(2, l.size());

		List<Object> sl1 = l.get(0);
		assertEquals(2, sl1.size());
		assertEquals("SS", sl1.get(0));
		assertEquals(55, sl1.get(1));

		List<Object> sl2 = l.get(1);
		assertEquals(1, sl2.size());
		assertEquals("X", sl2.get(0));

	}

	@Test
	public void testMap_SingleLevel() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("b1: SS\ni: 55".getBytes());

			return processor.apply(in);
		});

		Map<String, Object> m = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment,
				mockJacksonService).factory(new TypeReference<Map<String, Object>>() {
				}, "");

		assertNotNull(m);
		assertEquals("SS", m.get("b1"));
		assertEquals(55, m.get("i"));
	}

	@Test
	public void testMap_MultiLevel() {

		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream("b1:\n  k1: SS\n  i: 55".getBytes());

			return processor.apply(in);
		});

		Map<String, Map<String, Object>> m = new YamlFactoryConfigurationService(mockConfigSource, mockEnvironment,
				mockJacksonService).factory(new TypeReference<Map<String, Map<String, Object>>>() {
				}, "");

		assertNotNull(m);
		Map<String, Object> subM = m.get("b1");
		assertNotNull(subM);

		assertEquals("SS", subM.get("k1"));
		assertEquals(55, subM.get("i"));
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
