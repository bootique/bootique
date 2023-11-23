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

import io.bootique.cli.Cli;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * {@link Cli} implementation on top of jopt-simple library.
 */
public class JoptCli implements Cli {

	private final OptionSet optionSet;
	private final String commandName;

	public JoptCli(OptionSet parsed, String commandName) {
		this.optionSet = parsed;
		this.commandName = commandName;
	}

	@Override
	public String commandName() {
		return commandName;
	}

	@Override
	public boolean hasOption(String optionName) {
		return optionSet.has(optionName);
	}

	@Override
	public List<String> optionStrings(String name) {
		return optionSet.valuesOf(name).stream().map(String::valueOf).collect(toList());
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> standaloneArguments() {
		return (List<String>) optionSet.nonOptionArguments();
	}

    @Override
    public List<OptionSpec<?>> detectedOptions() {
        return optionSet != null ? optionSet.specs() : new ArrayList<>();
    }
}
