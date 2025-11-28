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

package io.bootique.jopt;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JoptCliTest {

    final OptionSet optionSet = new OptionParser(false).parse("");

	@Test
    public void stringsFor_Missing() {

		JoptCli opts = new JoptCli(optionSet, "aname");
		assertNotNull(opts.optionStrings("no_such_opt"));
		assertEquals(0, opts.optionStrings("no_such_opt").size());
	}

	@Test
    public void commandName() {
		JoptCli o1 = new JoptCli(optionSet, "aname");
		assertEquals("aname", o1.commandName());

		JoptCli o2 = new JoptCli(optionSet, null);
		assertNull(o2.commandName());
	}
}
