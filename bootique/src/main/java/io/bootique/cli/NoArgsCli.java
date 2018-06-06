/**
 *    Licensed to the ObjectStyle LLC under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ObjectStyle LLC licenses
 *  this file to you under the Apache License, Version 2.0 (the
 *  “License”); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package io.bootique.cli;

import joptsimple.OptionSpec;

import java.util.Collections;
import java.util.List;

/**
 * A Cli instance over an empty argument list.
 *
 * @since 0.25
 */
public final class NoArgsCli implements Cli {

    private static final Cli INSTANCE = new NoArgsCli();

    public static Cli getInstance() {
        return INSTANCE;
    }

    @Override
    public String commandName() {
        return null;
    }

    @Override
    public boolean hasOption(String name) {
        return false;
    }

    @Override
    public List<OptionSpec<?>> detectedOptions() {
        return Collections.emptyList();
    }

    @Override
    public List<String> optionStrings(String name) {
        return Collections.emptyList();
    }

    @Override
    public List<String> standaloneArguments() {
        return Collections.emptyList();
    }
}
