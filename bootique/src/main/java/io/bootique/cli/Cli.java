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

package io.bootique.cli;

import joptsimple.OptionSpec;

import java.util.List;

/**
 * Represents a set of command-line options passed to the Bootique app.
 */
public interface Cli {

    /**
     * Returns the name of the command to run, possibly derived from options or standalone arguments.
     *
     * @return a String that is a symbolic name of the command to run, derived from CLI options.
     */
    String commandName();

    boolean hasOption(String name);

    List<OptionSpec<?>> detectedOptions();

    /**
     * Returns a List of String values for the specified option name.
     *
     * @param name option name
     * @return a potentially empty collection of CLI values for a given option.
     */
    List<String> optionStrings(String name);

    /**
     * Returns a single value for option or null if not present.
     *
     * @param name option name.
     * @return a single value for option or null if not present.
     * @throws RuntimeException if there's more then one value for the option.
     */
    default String optionString(String name) {
        List<String> allStrings = optionStrings(name);

        if (allStrings.size() > 1) {
            throw new RuntimeException("More than one value specified for option: " + name);
        }

        return allStrings.isEmpty() ? null : allStrings.get(0);
    }

    /**
     * Returns all arguments that are not options or option values in the order
     * they are encountered on the command line.
     */
    List<String> standaloneArguments();
}
