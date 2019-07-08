/*
 * Licensed to ObjectStyle LLC under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ObjectStyle LLC licenses
 * this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.bootique;

import io.bootique.config.ConfigurationFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConfigModuleTest {

	@Test
	public void testDefaultConfigPrefix() {
		assertEquals("testxyz", new TestXyzModule().defaultConfigPrefix());
		assertEquals("xyz", new Xyz().defaultConfigPrefix());
	}

	@Test
	public void testConfigPrefix() {
		assertEquals("testxyz", new TestXyzModule().configPrefix);
		assertEquals("custom-prefix", new TestXyzModule("custom-prefix").configPrefix);
	}

	@Test
	public void testConfigConfig() {

		ConfigurationFactory f = mock(ConfigurationFactory.class);
		when(f.config(any(Class.class), anyString()))
				.then(a -> "_"+ a.getArgument(1));

		TestXyzModule m = new TestXyzModule();
		String v = m.config(String.class, f);
		assertEquals("_testxyz", v);
	}

	class TestXyzModule extends ConfigModule {

		public TestXyzModule() {

		}

		public TestXyzModule(String configPrefix) {
			super(configPrefix);
		}
	}

	class Xyz extends ConfigModule {

	}
}
