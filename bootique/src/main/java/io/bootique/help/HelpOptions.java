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

package io.bootique.help;

import io.bootique.meta.application.OptionMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class to collect CLI options from various models, sort them and resolve conflicts before including them in help.
 *
 * @since 0.20
 */
// TODO: this is in no way synchronized with JOpt parser, so we need extensive unit tests to verify that help behavior
// matches the actual runtime behavior.
public class HelpOptions {

    private Map<String, List<HelpOption>> byShortName;

    public HelpOptions() {
        this.byShortName = new HashMap<>();
    }

    public void add(OptionMetadata option) {
        HelpOption ho = new HelpOption(option);
        byShortName.computeIfAbsent(ho.getOption().getShortName(), sn -> new ArrayList<HelpOption>()).add(ho);
    }

    /**
     * Returns sorted options with resolved conflicting names.
     *
     * @return sorted options with resolved conflicting names.
     */
    public List<HelpOption> getOptions() {

        return byShortName.values().stream().flatMap(list -> {

            Stream<HelpOption> options = list.stream();

            // suppress short options if 2 or more long options resolve into a single short opt.
            // although if one of those options is 1 char long, it can be exposed as a short option...
            if (list.size() > 1) {

                boolean[] shortCounter = new boolean[1];

                options = options.map(o -> {

                    if (o.isLongNameAllowed()) {
                        o.setShortNameAllowed(false);
                    } else if (shortCounter[0]) {
                        throw new IllegalStateException("Conflicting short option name: " + o.getOption().getShortName());
                    } else {
                        shortCounter[0] = true;
                    }

                    return o;
                });
            }

            return options;
        }).sorted().collect(Collectors.toList());
    }
}
