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

import java.util.Map;
import java.util.Optional;

/**
 * A service interface that provides the rest of Bootique with access to available commands.
 *
 * @since 0.12
 */
public interface CommandManager {

    /**
     * Returns a map of {@link ManagedCommand} instances by command name, including all known commands: public, private,
     * default, help.
     *
     * @return a map of {@link ManagedCommand} instances by command name.
     * @since 0.25
     */
    Map<String, ManagedCommand> getAllCommands();

    /**
     * Returns a command matching the type. Throws an exception if the command type is not registered in the Bootique stack.
     *
     * @return a {@link ManagedCommand} matching the specified type.
     * @since 0.25
     */
    ManagedCommand lookupByType(Class<? extends Command> commandType);

    /**
     * Returns a command matching the name. Throws an exception if the command type is not registered in the Bootique stack.
     *
     * @return a {@link ManagedCommand} matching the specified type.
     * @since 0.25
     */
    default ManagedCommand lookupByName(String commandName) {
        ManagedCommand match = getAllCommands().get(commandName);

        if (match == null) {
            throw new IllegalArgumentException("Unknown command name: " + commandName);
        }

        return match;
    }

    /**
     * Returns optional public default command.
     *
     * @return optional public default command for this runtime.
     * @since 0.25
     */
    default Optional<Command> getPublicDefaultCommand() {

        for (ManagedCommand mc : getAllCommands().values()) {
            if (mc.isDefault() && !mc.isHidden()) {
                return Optional.of(mc.getCommand());
            }
        }

        return Optional.empty();
    }

    /**
     * Returns optional help command.
     *
     * @return optional help command for this runtime.
     * @since 0.20
     */
    default Optional<Command> getPublicHelpCommand() {
        for (ManagedCommand mc : getAllCommands().values()) {
            if (mc.isHelp() && !mc.isHidden()) {
                return Optional.of(mc.getCommand());
            }
        }

        return Optional.empty();
    }
}
