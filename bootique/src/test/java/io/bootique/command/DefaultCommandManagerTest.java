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

import io.bootique.cli.Cli;
import io.bootique.meta.application.CommandMetadata;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertSame;

public class DefaultCommandManagerTest {

    private Command c1 = new C1();
    private Command c2 = new C2();
    private Command c3 = new C3();

    @Test
    public void testLookupByType() {

        Map<String, ManagedCommand> map = new HashMap<>();
        map.put("x", ManagedCommand.forCommand(c1));
        map.put("y", ManagedCommand.forCommand(c2));
        map.put("z", ManagedCommand.builder(c3).asDefault().build());

        DefaultCommandManager cm = new DefaultCommandManager(map);
        assertSame(c1, cm.lookupByType(C1.class).getCommand());
        assertSame(c2, cm.lookupByType(C2.class).getCommand());
        assertSame("No default command included in lookup", c3, cm.lookupByType(C3.class).getCommand());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLookupByType_Missing() {

        Map<String, ManagedCommand> map = new HashMap<>();
        map.put("x", ManagedCommand.builder(c1).build());

        DefaultCommandManager cm = new DefaultCommandManager(map);
        cm.lookupByType(C2.class);
    }


    @Test
    public void testLookupByName() {

        Map<String, ManagedCommand> map = new HashMap<>();
        map.put("c1", ManagedCommand.forCommand(c1));
        map.put("c2", ManagedCommand.forCommand(c2));
        map.put("c3", ManagedCommand.builder(c3).asDefault().build());

        DefaultCommandManager cm = new DefaultCommandManager(map);
        assertSame(c1, cm.lookupByName("c1").getCommand());
        assertSame(c2, cm.lookupByName("c2").getCommand());
        assertSame("No default command included in lookup", c3, cm.lookupByName("c3").getCommand());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLookupByName_Missing() {

        Map<String, ManagedCommand> map = new HashMap<>();
        map.put("c1", ManagedCommand.forCommand(c1));

        DefaultCommandManager cm = new DefaultCommandManager(map);
        cm.lookupByName("c2");
    }

    static class C1 extends CommandWithMetadata {
        public C1() {
            super(CommandMetadata.builder(C1.class).build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static class C2 extends CommandWithMetadata {
        public C2() {
            super(CommandMetadata.builder(C2.class).build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }

    static class C3 extends CommandWithMetadata {
        public C3() {
            super(CommandMetadata.builder(C3.class).build());
        }

        @Override
        public CommandOutcome run(Cli cli) {
            return CommandOutcome.succeeded();
        }
    }
}
