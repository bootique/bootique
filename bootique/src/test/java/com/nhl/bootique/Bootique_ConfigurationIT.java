package com.nhl.bootique;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;

import com.nhl.bootique.config.ConfigurationFactory;
import com.nhl.bootique.type.TypeRef;
import com.nhl.bootique.unit.BQInternalTestFactory;

public class Bootique_ConfigurationIT {

	@ClassRule
	public static BQInternalTestFactory runtimeFactory = new BQInternalTestFactory();

	@Test
	public void testConfigConfig() {
		BQRuntime runtime = runtimeFactory.newRuntime().build("--config=src/test/resources/com/nhl/bootique/test1.yml",
				"--config=src/test/resources/com/nhl/bootique/test2.yml");

		Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
				.config(new TypeRef<Map<String, String>>() {
				}, "");
		assertEquals("{a=e, c=d}", config.toString());
	}

	@Test
	public void testConfigConfig_Reverse() {
		BQRuntime runtime = runtimeFactory.newRuntime().build("--config=src/test/resources/com/nhl/bootique/test2.yml",
				"--config=src/test/resources/com/nhl/bootique/test1.yml");

		Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
				.config(new TypeRef<Map<String, String>>() {
				}, "");
		assertEquals("{a=b, c=d}", config.toString());
	}

	@Test
	public void testConfigEnvOverrides() {
		BQRuntime runtime = runtimeFactory.newRuntime().var("BQ_A", "F")
				.build("--config=src/test/resources/com/nhl/bootique/test2.yml");

		Map<String, String> config = runtime.getInstance(ConfigurationFactory.class)
				.config(new TypeRef<Map<String, String>>() {
				}, "");

		assertEquals("{a=F, c=d}", config.toString());
	}

	@Test
	public void testConfigEnvOverrides_Nested() {
		BQRuntime runtime = runtimeFactory.newRuntime().var("BQ_A", "F").var("BQ_C_M_F", "F1").var("BQ_C_M_K", "3")
				.build("--config=src/test/resources/com/nhl/bootique/test3.yml");

		Bean1 b1 = runtime.getInstance(ConfigurationFactory.class).config(Bean1.class, "");

		assertEquals("F", b1.a);
		assertEquals(3, b1.c.m.k);
		assertEquals("n", b1.c.m.l);
		assertEquals("F1", b1.c.m.f);
	}

	static class Bean1 {
		private String a;
		private Bean2 c;

		public void setA(String a) {
			this.a = a;
		}

		public void setC(Bean2 c) {
			this.c = c;
		}
	}

	static class Bean2 {

		private Bean3 m;

		public void setM(Bean3 m) {
			this.m = m;
		}
	}

	static class Bean3 {
		private int k;
		private String f;
		private String l;

		public void setK(int k) {
			this.k = k;
		}

		public void setF(String f) {
			this.f = f;
		}

		public void setL(String l) {
			this.l = l;
		}
	}

}
