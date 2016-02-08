package com.nhl.bootique.config;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.bootique.env.Environment;
import com.nhl.bootique.jackson.JacksonService;
import com.nhl.bootique.type.TypeRef;

public class YamlConfigurationFactoryTest {

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

	private YamlConfigurationFactory factory(String yaml) {
		when(mockConfigSource.readConfig(any())).thenAnswer(i -> {

			@SuppressWarnings("unchecked")
			Function<InputStream, Object> processor = i.getArgumentAt(0, Function.class);
			InputStream in = new ByteArrayInputStream(yaml.getBytes());

			return processor.apply(in);
		});

		return new YamlConfigurationFactory(mockConfigSource, mockEnvironment, mockJacksonService);
	}

	@Test
	public void testConfig() {

		Bean1 b1 = factory("s: SS\ni: 55").config(Bean1.class, "");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testConfig_Nested() {
		Bean2 b2 = factory("b1:\n  s: SS\n  i: 55").config(Bean2.class, "");
		assertNotNull(b2);
		assertNotNull(b2.getB1());
		assertEquals("SS", b2.getB1().getS());
		assertEquals(55, b2.getB1().getI());
	}

	@Test
	public void testConfig_Subconfig() {
		Bean1 b1 = factory("b1:\n  s: SS\n  i: 55").config(Bean1.class, "b1");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testConfig_Subconfig_MultiLevel() {

		Bean1 b1 = factory("b0:\n  b1:\n    s: SS\n    i: 55").config(Bean1.class, "b0.b1");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testConfig_Subconfig_Missing() {
		Bean1 b1 = factory("b1:\n  s: SS\n  i: 55").config(Bean1.class, "no.such.path");
		assertNotNull(b1);
		assertEquals(null, b1.getS());
		assertEquals(0, b1.getI());
	}

	@Test
	public void testConfig_PropSubstitution() {

		when(mockEnvironment.frameworkProperties()).thenReturn(new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put("s", "SS");
				put("i", "55");
			}
		});

		Bean1 b1 = factory("s: replace_me\ni: replace_me").config(Bean1.class, "");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testConfig_PropSubstitution_Nested() {

		when(mockEnvironment.frameworkProperties()).thenReturn(new HashMap<String, String>() {
			private static final long serialVersionUID = 1L;

			{
				put("b1.s", "SS");
				put("b1.i", "55");
			}
		});

		Bean1 b1 = factory("b1:\n  s: replace_me\n  i: replace_me").config(Bean1.class, "b1");
		assertNotNull(b1);
		assertEquals("SS", b1.getS());
		assertEquals(55, b1.getI());
	}

	@Test
	public void testList_SingleLevel() {

		List<Object> l = factory("- SS\n- 55").config(new TypeRef<List<Object>>() {
		}, "");

		assertNotNull(l);
		assertEquals("SS", l.get(0));
		assertEquals(55, l.get(1));
	}

	@Test
	public void testList_MultiLevel() {

		List<List<Object>> l = factory("-\n  - SS\n  - 55\n-\n  - X").config(new TypeRef<List<List<Object>>>() {
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
		Map<String, Object> m = factory("b1: SS\ni: 55").config(new TypeRef<Map<String, Object>>() {
		}, "");

		assertNotNull(m);
		assertEquals("SS", m.get("b1"));
		assertEquals(55, m.get("i"));
	}

	@Test
	public void testMap_MultiLevel() {

		Map<String, Map<String, Object>> m = factory("b1:\n  k1: SS\n  i: 55")
				.config(new TypeRef<Map<String, Map<String, Object>>>() {
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
