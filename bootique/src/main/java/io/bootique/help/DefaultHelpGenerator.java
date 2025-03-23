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

import io.bootique.meta.application.ApplicationMetadata;
import io.bootique.meta.application.OptionMetadata;
import io.bootique.meta.config.ConfigValueMetadata;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Default {@link HelpGenerator} implementation.
 */
public class DefaultHelpGenerator implements HelpGenerator {

    private final ApplicationMetadata application;
    private final int lineWidth;

    public DefaultHelpGenerator(ApplicationMetadata application, int lineWidth) {
        this.application = application;
        this.lineWidth = lineWidth;
    }

    protected HelpAppender createAppender(Appendable out) {
        return new HelpAppender(createConsoleAppender(out));
    }

    protected ConsoleAppender createConsoleAppender(Appendable out) {
        return new ConsoleAppender(out, lineWidth);
    }


    @Override
    public void append(Appendable out) {

        HelpAppender appender = createAppender(out);

        printName(appender, application.getName(), application.getDescription());
        printOptions(appender, application.getCliOptions());
        printEnvironment(appender, application.getVariables());
    }

    protected void printName(HelpAppender out, String name, String description) {

        out.printSectionName("NAME");

        Objects.requireNonNull(name);

        out.printText(name);
        if (description != null) {
            out.printDescription(description);
        }
    }

    protected void printEnvironment(HelpAppender out, Collection<ConfigValueMetadata> variables) {
        if (variables.isEmpty()) {
            return;
        }

        out.printSectionName("ENVIRONMENT");

        Map<String, List<ConfigValueMetadata>> byName = variables.stream()

                // group repeating vars that map to different paths
                .collect(Collectors.groupingBy(ConfigValueMetadata::getName));

        byName.keySet().stream().sorted().forEach(k -> printVar(out, k, byName.get(k)));
    }

    protected void printVar(HelpAppender out, String varName, List<ConfigValueMetadata> varLinks) {
        out.printSubsectionHeader(varName);

        switch (varLinks.size()) {
            case 0: // unexpected
                break;
            case 1:
                String description = varLinks.get(0).getDescription();
                if (description != null) {
                    out.printDescription(description);
                }
                break;
            default:

                // print unique descriptions

                Set<String> descriptions = new LinkedHashSet<>();
                varLinks.forEach(v -> {
                    if (v.getDescription() != null) {
                        descriptions.add(v.getDescription());
                    }
                });

                boolean onePlus = false;
                for (String d : descriptions) {
                    if (onePlus) {
                        out.println();
                    }

                    onePlus = true;
                    out.printDescription(d);
                }
        }
    }

    protected void printOptions(HelpAppender out, Collection<OptionMetadata> options) {

        if (options.isEmpty()) {
            return;
        }

        out.printSectionName("OPTIONS");
        options.forEach(o -> {

            String valueName = o.getValueName();
            if (valueName == null || valueName.isEmpty()) {
                valueName = "val";
            }

            List<String> parts = new ArrayList<>();

            String shortName = o.getShortName() != null ? o.getShortName() : (o.getName().length() == 1 ? o.getName() : null);

            if (shortName != null) {
                parts.add("-");
                parts.add(shortName);

                switch (o.getValueCardinality()) {
                    case REQUIRED:
                        parts.add(" ");
                        parts.add(valueName);
                        break;
                    case OPTIONAL:
                        parts.add(" [");
                        parts.add(valueName);
                        parts.add("]");
                        break;
                }
            }

            if (o.getName().length() > 1) {

                if (!parts.isEmpty()) {
                    parts.add(", ");
                }

                parts.add("--");
                parts.add(o.getName());
                switch (o.getValueCardinality()) {
                    case REQUIRED:
                        parts.add("=");
                        parts.add(valueName);
                        break;
                    case OPTIONAL:
                        parts.add("[=");
                        parts.add(valueName);
                        parts.add("]");
                        break;
                }
            }

            out.printSubsectionHeader(parts);

            String description = o.getDescription();
            if (description != null) {
                out.printDescription(description);
            }
        });
    }
}
