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

package io.bootique;

import io.bootique.cli.Cli;
import io.bootique.command.Command;
import io.bootique.command.CommandOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class BQRuntimeListenerIT {

    static final AtomicInteger counter = new AtomicInteger(0);

    @BeforeEach
    void resetCounter() {
        counter.set(0);
    }

    @Test
    public void createRuntime() {

        Listener l1 = new Listener();
        Listener l2 = new Listener();
        Listener l3 = new Listener();

        BQRuntime rt = Bootique
                .app()
                .module(b -> BQCoreModule.extend(b).addRuntimeListener(l1).addRuntimeListener(l2).addRuntimeListener(l3))
                .createRuntime();

        assertSame(rt, l1.runtime);
        assertEquals(0, l1.order);

        assertSame(rt, l2.runtime);
        assertEquals(1, l2.order);

        assertSame(rt, l3.runtime);
        assertEquals(2, l3.order);
    }

    @Test
    public void exec() {

        Listener l1 = new Listener();
        XCommand c1 = new XCommand();

        CommandOutcome o = Bootique
                .app()
                .module(b -> BQCoreModule.extend(b).addRuntimeListener(l1))
                .module(b -> BQCoreModule.extend(b).setDefaultCommand(c1))
                .exec();

        assertTrue(o.isSuccess());
        assertNotNull(l1.runtime);
        assertEquals(0, l1.order, "Listener must have been called prior to the command");
        assertEquals(1, c1.order);
    }

    static class Listener implements BQRuntimeListener {

        BQRuntime runtime;
        int order;

        @Override
        public void onRuntimeCreated(BQRuntime runtime) {
            this.runtime = runtime;
            this.order = BQRuntimeListenerIT.counter.getAndIncrement();
        }
    }

    static class XCommand implements Command {

        int order;

        @Override
        public CommandOutcome run(Cli cli) {
            this.order = BQRuntimeListenerIT.counter.getAndIncrement();
            return CommandOutcome.succeeded();
        }
    }
}
