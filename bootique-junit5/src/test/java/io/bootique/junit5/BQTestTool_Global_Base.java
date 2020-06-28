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
package io.bootique.junit5;

import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.fail;

@BQTest
public abstract class BQTestTool_Global_Base {

    @BQTestTool(BQTestScope.GLOBAL)
    protected static final BQTestToolCallbackRecorder globalScope = new BQTestToolCallbackRecorder();

    protected static final AtomicInteger subclassCounter = new AtomicInteger();

    @BeforeAll
    static void beforeAll() {
        subclassCounter.incrementAndGet();
    }

    protected void assertState() {

        globalScope.assertCallbacksScope(BQTestScope.GLOBAL);

        int n = subclassCounter.get();

        switch (n) {
            case 1:
                globalScope.assertCallbacksSize("beforeScope", 1);
                globalScope.assertCallbacksSize("beforeMethod", 1);
                globalScope.assertCallbacksSize("afterMethod", 0);
                globalScope.assertCallbacksSize("afterScope", 0);
                break;
            case 2:
                globalScope.assertCallbacksSize("beforeScope", 1);
                globalScope.assertCallbacksSize("beforeMethod", 2);
                globalScope.assertCallbacksSize("afterMethod", 1);
                globalScope.assertCallbacksSize("afterScope", 0);
                break;
            default:
                fail("Unexpected number of subclasses executed: " + n);
                break;
        }
    }
}
