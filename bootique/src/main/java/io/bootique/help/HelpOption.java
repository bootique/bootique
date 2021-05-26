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

import java.util.Objects;

/**
 * A wrapper for {@link OptionMetadata} that handles option sorting, short names and conflicts.
 */
public class HelpOption implements Comparable<HelpOption> {

    private OptionMetadata option;
    private boolean shortNameAllowed;
    private boolean longNameAllowed;

    public HelpOption(OptionMetadata option) {
        this.option = Objects.requireNonNull(option);
        this.shortNameAllowed = true;
        this.longNameAllowed = option.getName().length() > 1;
    }

    @Override
    public int compareTo(HelpOption o) {
        return option.getName().compareTo(o.getOption().getName());
    }

    public OptionMetadata getOption() {
        return option;
    }

    public boolean isShortNameAllowed() {
        return shortNameAllowed;
    }

    public boolean isLongNameAllowed() {
        return longNameAllowed;
    }

    public void setShortNameAllowed(boolean shortNameAllowed) {
        this.shortNameAllowed = shortNameAllowed;
    }
}
