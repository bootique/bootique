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

import java.util.HashMap;
import java.util.Map;

/**
 * @since 0.12
 */
public class DefaultCommandManager implements CommandManager {

    private final Map<String, ManagedCommand> commands;
    private volatile Map<Class, ManagedCommand> commandsByType;

    public DefaultCommandManager(Map<String, ManagedCommand> commands) {
        this.commands = commands;
    }

    @Override
    public Map<String, ManagedCommand> getAllCommands() {
        return commands;
    }

    @Override
    public ManagedCommand lookupByType(Class<? extends Command> commandType) {

        ManagedCommand match = commandsByType().get(commandType);
        if (match == null) {
            throw new IllegalArgumentException("Unknown command type: " + commandType.getName());
        }
        return match;
    }

    private Map<Class, ManagedCommand> commandsByType() {
        // lookup by class is an edge case used by command decorators and such, so create index on demand

        if (commandsByType == null) {
            synchronized (this) {
                if (commandsByType == null) {
                    commandsByType = createCommandsByType();
                }
            }
        }

        return commandsByType;
    }

    private Map<Class, ManagedCommand> createCommandsByType() {
        Map<Class, ManagedCommand> map = new HashMap<>((int) (commands.size() / 0.75));
        commands.values().forEach(mc -> map.put(mc.getCommand().getClass(), mc));
        return map;
    }
}
