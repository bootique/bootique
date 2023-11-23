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

package io.bootique.command;

import io.bootique.meta.application.CommandMetadata;
import io.bootique.cli.Cli;

/**
 * An abstract superlcass of commands that provide their own metadata.
 */
public abstract class CommandWithMetadata implements Command {

    private final CommandMetadata metadata;

    /**
     * @deprecated in favor of {@link CommandWithMetadata#CommandWithMetadata(CommandMetadata)}
     */
    @Deprecated(since = "3.0", forRemoval = true)
    public CommandWithMetadata(CommandMetadata.Builder metadataBuilder) {
        this(metadataBuilder.build());
    }

    public CommandWithMetadata(CommandMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public CommandMetadata getMetadata() {
        return metadata;
    }

    @Override
    public abstract CommandOutcome run(Cli cli);
}
