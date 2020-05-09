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
package io.bootique.test.junit5;

import io.bootique.BQRuntime;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * @since 2.0
 */
public class TestRuntimesManager {

    private Collection<BQRuntime> runtimes;

    public void add(BQRuntime runtime) {
        Objects.requireNonNull(runtimes, "'runtimes' not initialized. Called outside factory lifecycle?");
        this.runtimes.add(runtime);
    }

    protected int size() {
        return runtimes != null ? runtimes.size() : 0;
    }

    public void reset() {
        this.runtimes = new ArrayList<>();
    }

    public void shutdown() {
        Collection<BQRuntime> localRuntimes = this.runtimes;

        if (localRuntimes != null) {
            localRuntimes.forEach(runtime -> {
                try {
                    runtime.shutdown();
                } catch (Exception e) {
                    // ignore...
                }
            });
        }
    }
}
